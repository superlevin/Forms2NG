export interface MenuEntry {
  formsName: string;
}

export interface FormsStats {
  numberOfBlocks: number;
  numberOfTriggers: number;
  numberOfItems: number;
  numberOfCanvas: number;
}

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

export interface Canvas {
  name: string;
  comment?: string;
}

export interface Trigger {
  name: string;
  triggerText?: string;
  comment?: string;
  executeHierarchy?: string;
}

export interface Item {
  name: string;
}
