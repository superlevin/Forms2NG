# Forms2NG

**Forms2NG** 可讓你在 Web 瀏覽器中分析 Oracle Forms / D2k 模組檔案（`FMB`、`MMB`、`OLB`），而不需要 Oracle Forms Builder。

## 技術堆疊（v2）

| 層級 | 技術 |
|-------|-----------|
| Frontend | Angular 18、TypeScript、Bootstrap 5 |
| Backend  | Spring Boot 3.3、Java 17、Spring MVC |
| XML parsing | Jakarta JAXB（Oracle Forms XML schema） |
| Build | npm / Angular CLI 18、Maven 3.x |

## 專案

- **Forms2NgWeb** – Angular 18 單頁應用程式
- **Forms2NgWebService** – Spring Boot 3 REST API（可執行 JAR）

## 支援的 D2k 格式

Oracle Forms XML 檔案是由 Oracle Forms 內建的 `frmf2xml` / `f2xml` 工具所產生。

| 格式 | 檔案尾碼 | 說明 | API 前綴 |
|--------|------------|-------------|------------|
| FMB    | `*_fmb.xml` | Form Module | `/rest/forms/`、`/rest/menu/` |
| MMB    | `*_mmb.xml` | Menu Module | `/rest/d2k/mmb/`、`/rest/menu/mmb` |
| OLB    | `*_olb.xml` | Object Library | `/rest/d2k/olb/`、`/rest/menu/olb` |
| PLL    | 於 FMB/MMB 內被參照 | PL/SQL Library（僅參照） | — |

## Backend 設定

### 先決條件
- Java 17+
- Maven 3.8+

### 設定（`application.properties`）

| 屬性 | 預設值 | 說明 |
|----------|---------|-------------|
| `forms2ng.path` | `/opt/forms2ng/` | 存放 `*_fmb.xml`、`*_mmb.xml`、`*_olb.xml` 檔案的目錄 |
| `forms2ng.cors.allowed-origins` | `*` | 允許的 CORS 來源（例如 `http://localhost:4200`） |
| `server.port` | `8080` | HTTP 連接埠 |
| `server.servlet.context-path` | `/forms2ng` | 應用程式路徑前綴 |

你可以透過環境變數或自訂 `application.properties` 覆寫任一屬性：
```bash
export FORMS2NG_PATH=/data/forms/xml/
export SERVER_PORT=9090
java -jar forms2ng-service-2.0.0.jar
```

### 建置與執行
```bash
cd Forms2NgWebService
mvn clean package -DskipTests
java -jar target/forms2ng-service-2.0.0.jar --forms2ng.path=/path/to/xml/files/
```

### REST API

| 方法 | 路徑 | 說明 |
|--------|------|-------------|
| GET | `/rest/menu/` | 列出 FMB 模組 |
| GET | `/rest/menu/mmb` | 列出 MMB 模組 |
| GET | `/rest/menu/olb` | 列出 OLB 模組 |
| GET | `/rest/forms/stats/{name}` | 表單統計資訊（blocks、items、triggers、canvases） |
| GET | `/rest/forms/blocks/{name}` | Block 清單 |
| GET | `/rest/forms/canvas/{name}` | Canvas 清單 |
| GET | `/rest/forms/triggersforblock/{name}/{block}` | 指定 block 的 triggers |
| GET | `/rest/blocks/{name}/{block}` | 單一 block 詳細資訊 |
| GET | `/rest/d2k/mmb/{name}` | 完整 MMB 模組 |
| GET | `/rest/d2k/olb/{name}` | 完整 OLB 模組 |

錯誤回應使用標準 HTTP 狀態碼：
- `404 Not Found` – 在設定的路徑中找不到檔案
- `500 Internal Server Error` – XML 解析錯誤

## Frontend 設定

### 先決條件
- Node.js 20 LTS+
- npm 10+

### 設定
編輯 `src/environments/environment.ts`（開發）或 `src/environments/environment.prod.ts`（正式）：
```typescript
export const environment = {
  production: false,
  apiBase: 'http://localhost:8080/forms2ng'
};
```

### 開發伺服器
```bash
cd Forms2NgWeb
npm install
npm start           # 服務位址為 http://localhost:4200
```

### 正式版建置
```bash
npm run build:prod  # 輸出於 dist/forms2ng-web/
```

### 測試
```bash
npm test            # 在 ChromeHeadless 中執行 Karma + Jasmine
```

## 準備 XML 檔案

使用 Oracle 的 `frmf2xml` 工具將 Oracle Forms 二進位檔轉換為 XML：
```bash
# 轉換 FMB（form）
frmf2xml MYFORM.fmb
# 會產生 MYFORM_fmb.xml

# 轉換 MMB（menu module）
frmf2xml MYMENU.mmb
# 會產生 MYMENU_mmb.xml

# 轉換 OLB（object library）
frmf2xml MYLIB.olb
# 會產生 MYLIB_olb.xml
```

請將所有 `*_fmb.xml`、`*_mmb.xml`、`*_olb.xml` 檔案放入 `forms2ng.path` 所設定的目錄中。

## 舊版 Frontend

原始的 AngularJS 1.2 Frontend 保留於 `Forms2NgWeb/app-legacy/`，可供參考。
