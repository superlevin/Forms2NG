# D2K FORM 解析核心技術說明

## 目錄

1. [背景與概述](#1-背景與概述)
2. [D2K FORM 格式說明](#2-d2k-form-格式說明)
3. [二進位轉 XML：frmf2xml](#3-二進位轉-xmlfrmf2xml)
4. [Oracle Forms XML Schema](#4-oracle-forms-xml-schema)
5. [JAXB 物件模型](#5-jaxb-物件模型)
6. [解析管線（Parsing Pipeline）](#6-解析管線parsing-pipeline)
7. [核心物件詳解](#7-核心物件詳解)
8. [REST API 設計](#8-rest-api-設計)
9. [安全考量](#9-安全考量)
10. [延伸：支援 MMB 與 OLB](#10-延伸支援-mmb-與-olb)

---

## 1. 背景與概述

Oracle Forms（又稱 D2K，Developer 2000）是 Oracle 提供的用戶端/伺服器快速應用程式開發工具。  
其模組以二進位格式儲存（`.fmb`、`.mmb`、`.olb`），需要 Oracle Forms Builder 才能開啟。

**Forms2NG** 的核心目的：在不安裝 Oracle Forms Builder 的情況下，透過瀏覽器解析並瀏覽 D2K 模組的結構資訊。

解析流程概覽：

```
[.fmb / .mmb / .olb]
        │
        ▼  (Oracle frmf2xml 工具)
[*_fmb.xml / *_mmb.xml / *_olb.xml]
        │
        ▼  (Spring Boot + Jakarta JAXB)
[Java 物件樹 (Module → FormModule → Block → Item ...)]
        │
        ▼  (Spring MVC REST API)
[JSON 回應]
        │
        ▼  (Angular 18 前端)
[瀏覽器畫面]
```

---

## 2. D2K FORM 格式說明

| 格式 | 副檔名 | XML 轉換後尾碼 | 說明 |
|------|--------|---------------|------|
| Form Module | `.fmb` | `*_fmb.xml` | 主表單模組，包含 Blocks、Items、Triggers、Canvas 等 |
| Menu Module | `.mmb` | `*_mmb.xml` | 選單模組，包含 Menu、MenuItem 等 |
| Object Library | `.olb` | `*_olb.xml` | 可重用物件函式庫，包含 ObjectLibraryTab 等 |
| PL/SQL Library | `.pll` | 僅被參照 | PL/SQL 程式庫（本工具不直接解析） |

---

## 3. 二進位轉 XML：frmf2xml

Oracle Forms 內建的 `frmf2xml`（或 `f2xml`）命令列工具可將二進位模組轉換為標準 XML 格式。

```bash
# 轉換 Form Module (.fmb → *_fmb.xml)
frmf2xml MYFORM.fmb
# 輸出：MYFORM_fmb.xml

# 轉換 Menu Module (.mmb → *_mmb.xml)
frmf2xml MYMENU.mmb
# 輸出：MYMENU_mmb.xml

# 轉換 Object Library (.olb → *_olb.xml)
frmf2xml MYLIB.olb
# 輸出：MYLIB_olb.xml
```

轉換後的 XML 使用 Oracle 官方命名空間：

```xml
xmlns="http://xmlns.oracle.com/Forms"
```

所有元素與屬性均定義在此命名空間下。

---

## 4. Oracle Forms XML Schema

轉換後的 XML 結構遵循 Oracle Forms 官方 XSD Schema，命名空間為 `http://xmlns.oracle.com/Forms`。

### 4.1 根元素結構

```xml
<Module version="...">
  <!-- 三擇一 -->
  <FormModule ...>...</FormModule>
  <!-- 或 -->
  <MenuModule ...>...</MenuModule>
  <!-- 或 -->
  <ObjectLibrary ...>...</ObjectLibrary>
</Module>
```

### 4.2 FormModule 子元素（擇多）

`FormModule` 使用 `<xs:choice maxOccurs="unbounded">` 混合排列下列元素：

| 元素 | 說明 |
|------|------|
| `Coordinate` | 座標系統設定 |
| `Alert` | 警示對話框定義 |
| `AttachedLibrary` | 附加的 PLL 函式庫參照 |
| `Block` | 資料區塊（對應資料表/視圖） |
| `Canvas` | 畫面版面定義 |
| `Editor` | 文字編輯器定義 |
| `LOV` | 值清單（List of Values） |
| `ModuleParameter` | 模組參數 |
| `ObjectGroup` | 物件群組 |
| `ProgramUnit` | PL/SQL 程式單元 |
| `PropertyClass` | 屬性類別 |
| `RecordGroup` | 記錄群組（靜態或動態查詢） |
| `Report` | 報表物件 |
| `Trigger` | 表單層級觸發器 |
| `VisualAttribute` | 視覺屬性 |
| `Window` | 視窗定義 |

### 4.3 Block 子元素

| 元素 | 說明 |
|------|------|
| `DataSourceArgument` | 資料來源參數 |
| `DataSourceColumn` | 資料來源欄位 |
| `Item` | 畫面項目（欄位、按鈕、核取方塊等） |
| `Relation` | Block 間的關聯關係 |
| `Trigger` | Block 層級觸發器 |

### 4.4 Item 子元素

| 元素 | 說明 |
|------|------|
| `RadioButton` | 單選按鈕選項 |
| `Trigger` | Item 層級觸發器 |
| `ListItemElement` | 下拉清單選項 |

---

## 5. JAXB 物件模型

Forms2NG 使用 **Jakarta JAXB**（`jakarta.xml.bind`）將 XML 自動反序列化為 Java 物件。  
相關類別位於 `com.mobissime.forms.objects` 套件，均由 JAXB xjc 工具依 Oracle Forms XSD 自動產生。

### 5.1 命名空間宣告（package-info.java）

```java
@XmlSchema(
    namespace = "http://xmlns.oracle.com/Forms",
    elementFormDefault = XmlNsForm.QUALIFIED
)
package com.mobissime.forms.objects;
```

此設定確保所有元素均使用 Oracle Forms 命名空間進行匹配。

### 5.2 根物件 Module

```java
@XmlRootElement(name = "Module")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Module", propOrder = {"formModule", "objectLibrary", "menuModule"})
public class Module {
    @XmlElement(name = "FormModule")
    protected FormModule formModule;

    @XmlElement(name = "ObjectLibrary")
    protected ObjectLibrary objectLibrary;

    @XmlElement(name = "MenuModule")
    protected MenuModule menuModule;

    @XmlAttribute(name = "version")
    protected Long version;
}
```

`Module` 是 JAXB 的進入點，對應 XML 根元素 `<Module>`，內含三種模組類型擇一。

### 5.3 物件繼承關係圖

```
Module
├── FormModule
│   ├── List<Object> coordinateOrAlertOrAttachedLibrary
│   │   ├── Coordinate
│   │   ├── Alert
│   │   ├── AttachedLibrary
│   │   ├── Block
│   │   │   └── List<Object> dataSourceArgumentOrDataSourceColumnOrItem
│   │   │       ├── DataSourceArgument
│   │   │       ├── DataSourceColumn
│   │   │       ├── Item
│   │   │       │   └── List<Object> radioButtonOrTriggerOrListItemElement
│   │   │       │       ├── RadioButton
│   │   │       │       ├── Trigger
│   │   │       │       └── ListItemElement
│   │   │       ├── Relation
│   │   │       └── Trigger
│   │   ├── Canvas
│   │   │   ├── Graphics
│   │   │   ├── TabPage
│   │   │   └── VisualState
│   │   ├── LOV
│   │   ├── ProgramUnit
│   │   ├── Trigger
│   │   ├── Window
│   │   └── ...（其餘頂層物件）
├── MenuModule
│   └── List<Object> menuOrProgramUnitOrAttachedLibrary
│       ├── Menu
│       │   └── MenuItem
│       └── ...
└── ObjectLibrary
    └── List<ObjectLibraryTab>
```

### 5.4 混合子元素列表的處理方式

由於 Oracle Forms XML 採用 `<xs:choice maxOccurs="unbounded">` 設計，各層級的子元素以 **單一 `List<Object>`** 儲存，並使用 `@XmlElements` 標註多型映射：

```java
// FormModule 中的混合子元素列表
@XmlElements({
    @XmlElement(name = "Coordinate", type = Coordinate.class),
    @XmlElement(name = "Alert",      type = Alert.class),
    @XmlElement(name = "Block",      type = Block.class),
    @XmlElement(name = "Canvas",     type = Canvas.class),
    // ...其餘元素
})
protected List<Object> coordinateOrAlertOrAttachedLibrary;
```

遍歷時需以 `instanceof` 篩選目標型別：

```java
for (Object o : form.getCoordinateOrAlertOrAttachedLibrary()) {
    if (o instanceof Block b) {
        // 處理 Block
    } else if (o instanceof Canvas c) {
        // 處理 Canvas
    }
}
```

---

## 6. 解析管線（Parsing Pipeline）

核心解析邏輯集中於 `FormsParserService`（`com.mobissime.forms.service`）。

### 6.1 JAXBContext 初始化

`JAXBContext` 在 Spring Bean 建構時**僅初始化一次**，效能最佳：

```java
@Service
public class FormsParserService {
    private final JAXBContext jaxbContext;

    public FormsParserService(FormsProperties props) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(Module.class);
    }
}
```

### 6.2 路徑安全驗證（Path Traversal 防護）

讀取檔案前先驗證模組名稱與路徑：

```java
private static final Pattern SAFE_MODULE_NAME = Pattern.compile("[A-Za-z0-9._-]+");

private File resolveFile(String name, String suffix) throws FileNotFoundException {
    // 1. 白名單驗證模組名稱
    if (!SAFE_MODULE_NAME.matcher(name).matches()) {
        throw new FileNotFoundException("File not found: invalid module name");
    }
    // 2. 將 basePath 與 resolvedPath 正規化，確保未逃逸出基底目錄
    Path basePath = Paths.get(props.getFormsPath()).toAbsolutePath().normalize();
    Path resolvedPath = basePath.resolve(name + suffix).normalize();
    if (!resolvedPath.startsWith(basePath) || !Files.isRegularFile(resolvedPath)) {
        throw new FileNotFoundException("File not found: " + resolvedPath);
    }
    return resolvedPath.toFile();
}
```

### 6.3 XML 解析（XXE 防護）

使用 `XMLInputFactory` 停用 DTD 與外部實體，防止 XXE（XML External Entity）攻擊：

```java
private Module parseModule(String name, String suffix) throws Exception {
    Unmarshaller u = jaxbContext.createUnmarshaller();

    XMLInputFactory inputFactory = XMLInputFactory.newFactory();
    // 停用 DTD 支援
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    // 停用外部實體
    inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

    try (InputStream inputStream = Files.newInputStream(resolveFile(name, suffix).toPath())) {
        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(inputStream);
        try {
            // JAXB Unmarshaller 使用 StAX 讀取器進行反序列化
            return (Module) u.unmarshal(xmlStreamReader);
        } finally {
            xmlStreamReader.close();
        }
    }
}
```

### 6.4 模組取得方法

| 方法 | 檔案尾碼 | 回傳型別 |
|------|---------|---------|
| `getFormModule(name)` | `_fmb.xml` | `FormModule` |
| `getMenuModule(name)` | `_mmb.xml` | `MenuModule` |
| `getObjectLibrary(name)` | `_olb.xml` | `ObjectLibrary` |

### 6.5 完整解析流程圖

```
HTTP GET /rest/forms/blocks/{name}
        │
        ▼
FormsController.getBlocks(formName)
        │
        ▼
FormsParserService.getBlocks(formName)
        │
        ├── resolveFile(name, "_fmb.xml")   ← 路徑安全驗證
        │
        ├── XMLInputFactory（停用 DTD / 外部實體）
        │
        ├── JAXBContext.createUnmarshaller()
        │
        ├── Unmarshaller.unmarshal(XMLStreamReader) → Module
        │
        ├── module.getFormModule()           ← 取得 FormModule
        │
        └── 遍歷 coordinateOrAlertOrAttachedLibrary
                │
                └── instanceof Block → 加入 List<Block>
                        │
                        ▼
              JSON 序列化 → HTTP 回應
```

---

## 7. 核心物件詳解

### 7.1 Block（資料區塊）

對應資料庫中的一張資料表或視圖。關鍵屬性：

| 屬性 | 說明 |
|------|------|
| `Name` | Block 名稱（必填） |
| `DatabaseBlock` | 是否為資料庫 Block |
| `QueryDataSourceType` | 資料來源類型（TABLE、PROCEDURE 等） |
| `DMLDataName` | 對應的資料表或 PROCEDURE 名稱 |
| `Alias` | 資料表別名 |
| `KeyMode` | 主鍵模式 |
| `InsertAllowed` / `UpdateAllowed` / `DeleteAllowed` / `QueryAllowed` | DML 權限 |

子元素：`Item`（欄位）、`Trigger`（觸發器）、`Relation`（關聯）、`DataSourceColumn`

### 7.2 Item（項目/欄位）

對應畫面上的一個輸入欄位、按鈕、核取方塊等控制項。關鍵屬性：

| 屬性 | 說明 |
|------|------|
| `Name` | Item 名稱（必填） |
| `ItemType` | 項目類型（TEXT_ITEM、BUTTON、CHECK_BOX、LIST 等） |
| `DatabaseItem` | 是否對應資料庫欄位 |
| `PrimaryKey` | 是否為主鍵 |
| `DataType` | 資料型別（CHAR、NUMBER、DATE 等） |
| `MaximumLength` | 最大長度 |
| `Required` | 是否必填 |
| `CanvasName` | 所屬 Canvas 名稱 |
| `XPosition` / `YPosition` | 畫面座標 |
| `Width` / `Height` | 尺寸 |

### 7.3 Trigger（觸發器）

PL/SQL 事件處理器，可附加在 Form、Block 或 Item 三個層級。關鍵屬性：

| 屬性 | 說明 |
|------|------|
| `Name` | 觸發器名稱（如 `WHEN-BUTTON-PRESSED`、`PRE-QUERY`） |
| `TriggerText` | PL/SQL 程式碼內容 |
| `TriggerStyle` | 執行風格（`PL/SQL`、`V2`） |
| `ExecuteHierarchy` | 執行階層（`OVERRIDE`、`BEFORE`、`AFTER`） |
| `FireInQuery` | 是否在查詢模式觸發 |

### 7.4 Canvas（畫布）

定義視窗中的版面配置區域。子元素包含 `Graphics`（圖形物件）、`TabPage`（頁籤頁面）。

| 屬性 | 說明 |
|------|------|
| `Name` | Canvas 名稱 |
| `CanvasType` | 類型（`CONTENT`、`STACKED`、`TAB`、`TOOLBAR` 等） |
| `Width` / `Height` | 尺寸 |
| `ViewportWidth` / `ViewportHeight` | 可視區域大小 |

### 7.5 Trigger 程式碼的特殊字元處理

Oracle Forms XML 中，PL/SQL 程式碼內的換行符號以 `&#10;`（十進位）表示。  
在 REST API 回應前需轉換為前端可顯示的格式：

```java
// FormsController.java
triggers.forEach(t -> {
    if (t.getTriggerText() != null) {
        t.setTriggerText(t.getTriggerText().replace("&#10;", "<br />"));
    }
});
```

---

## 8. REST API 設計

後端採用 **Spring MVC `@RestController`**，JSON 序列化由 Jackson 自動處理。

### 8.1 FormsController（`/rest/forms`）

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/rest/forms/stats/{name}` | 取得表單統計（blocks、items、triggers、canvases 數量） |
| GET | `/rest/forms/blocks/{name}` | 取得所有 Block 清單（含 trigger 數量） |
| GET | `/rest/forms/canvas/{name}` | 取得所有 Canvas 清單 |
| GET | `/rest/forms/triggersforblock/{name}/{block}` | 取得指定 Block 的 Trigger 清單 |

### 8.2 BlockController（`/rest/blocks`）

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/rest/blocks/{name}/{block}` | 取得單一 Block 完整詳細資訊 |

### 8.3 D2kController（`/rest/d2k`）

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/rest/d2k/mmb/{name}` | 取得完整 Menu Module 物件 |
| GET | `/rest/d2k/olb/{name}` | 取得完整 Object Library 物件 |

### 8.4 MenuController（`/rest/menu`）

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/rest/menu/` | 列出所有可用的 FMB 模組名稱 |
| GET | `/rest/menu/mmb` | 列出所有可用的 MMB 模組名稱 |
| GET | `/rest/menu/olb` | 列出所有可用的 OLB 模組名稱 |

### 8.5 錯誤處理

| HTTP 狀態碼 | 觸發條件 |
|------------|---------|
| `404 Not Found` | 在 `forms2ng.path` 目錄中找不到對應的 XML 檔案 |
| `500 Internal Server Error` | XML 解析失敗（格式錯誤、不符合 Schema 等） |

---

## 9. 安全考量

### 9.1 路徑穿越（Path Traversal）防護

- 模組名稱以白名單正規表示式 `[A-Za-z0-9._-]+` 驗證，拒絕包含 `/`、`..` 等危險字元。
- 解析後的絕對路徑以 `startsWith(basePath)` 確認未逃逸出基底目錄。

### 9.2 XXE（XML External Entity）防護

- `XMLInputFactory` 明確停用 `SUPPORT_DTD` 與 `IS_SUPPORTING_EXTERNAL_ENTITIES`。
- 使用 StAX `XMLStreamReader` 傳入 JAXB，不直接解析 `File` 或 `URL`，避免隱性 DTD 載入。

### 9.3 CORS 設定

- 允許的來源由 `forms2ng.cors.allowed-origins` 屬性控制，正式環境建議設為具體來源而非 `*`。

---

## 10. 延伸：支援 MMB 與 OLB

### 10.1 MenuModule 結構

```
MenuModule
└── List<Object> menuOrProgramUnitOrAttachedLibrary
    ├── Menu
    │   └── List<MenuItem>
    ├── ProgramUnit（PL/SQL 程式單元）
    └── AttachedLibrary（附加函式庫）
```

### 10.2 ObjectLibrary 結構

```
ObjectLibrary
└── List<ObjectLibraryTab>（頁籤）
    └── 各頁籤中的可重用物件
```

解析方式與 `FormModule` 完全相同，均透過 `FormsParserService.parseModule()` 後取得對應子物件即可。

---

## 參考資料

- Oracle Forms XML Schema 命名空間：`http://xmlns.oracle.com/Forms`
- Jakarta JAXB API：`jakarta.xml.bind`
- StAX API：`javax.xml.stream.XMLInputFactory`
- 原始碼位置：
  - 解析服務：`Forms2NgWebService/src/main/java/com/mobissime/forms/service/FormsParserService.java`
  - 物件模型：`Forms2NgWebService/src/main/java/com/mobissime/forms/objects/`
  - REST 控制器：`Forms2NgWebService/src/main/java/com/mobissime/forms/controller/`
