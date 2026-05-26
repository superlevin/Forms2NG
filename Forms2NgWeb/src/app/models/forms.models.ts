/**
 * 表示後端選單 API 回傳的模組項目名稱。
 */
export interface MenuEntry {
  formsName: string;
}

/**
 * 表示單一表單的統計資訊摘要。
 */
export interface FormsStats {
  numberOfBlocks: number;
  numberOfTriggers: number;
  numberOfItems: number;
  numberOfCanvas: number;
}

/**
 * 表示 Oracle Forms 中的 block 定義與其主要屬性。
 */
export interface Block {
  name: string;
  numberOfTriggers?: number;
  databaseBlock?: boolean;
  queryAllowed?: boolean;
  insertAllowed?: boolean;
  updateAllowed?: boolean;
  deleteAllowed?: boolean;
  alias?: string;
  comment?: string;
  queryDataSourceName?: string;
}

/**
 * 表示表單中的 canvas 資訊。
 */
export interface Canvas {
  name: string;
  comment?: string;
}

/**
 * 表示掛載於 block 或表單元素上的 trigger 定義。
 */
export interface Trigger {
  name: string;
  triggerText?: string;
  comment?: string;
  executeHierarchy?: string;
}

/**
 * 表示表單中的基本項目識別資訊。
 */
export interface Item {
  name: string;
}
