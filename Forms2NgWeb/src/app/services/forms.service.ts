import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Block, Canvas, FormsStats, MenuEntry, Trigger } from '../models/forms.models';

/**
 * 封裝 Forms2NG 後端 REST API 的存取邏輯，
 * 提供表單、選單模組、物件函式庫與明細資料的查詢方法。
 */
@Injectable({ providedIn: 'root' })
export class FormsService {
  private readonly base = environment.apiBase;

  constructor(private readonly http: HttpClient) {}

  getFormsList(): Observable<MenuEntry[]> {
    return this.http.get<MenuEntry[]>(`${this.base}/rest/menu/`);
  }

  getMenuModuleList(): Observable<MenuEntry[]> {
    return this.http.get<MenuEntry[]>(`${this.base}/rest/menu/mmb`);
  }

  getObjectLibraryList(): Observable<MenuEntry[]> {
    return this.http.get<MenuEntry[]>(`${this.base}/rest/menu/olb`);
  }

  getStats(formName: string): Observable<FormsStats> {
    return this.http.get<FormsStats>(`${this.base}/rest/forms/stats/${encodeURIComponent(formName)}`);
  }

  getBlocks(formName: string): Observable<Block[]> {
    return this.http.get<Block[]>(`${this.base}/rest/forms/blocks/${encodeURIComponent(formName)}`);
  }

  getCanvases(formName: string): Observable<Canvas[]> {
    return this.http.get<Canvas[]>(`${this.base}/rest/forms/canvas/${encodeURIComponent(formName)}`);
  }

  getTriggersForBlock(formName: string, blockName: string): Observable<Trigger[]> {
    return this.http.get<Trigger[]>(
      `${this.base}/rest/forms/triggersforblock/${encodeURIComponent(formName)}/${encodeURIComponent(blockName)}`
    );
  }

  getBlock(formName: string, blockName: string): Observable<Block> {
    return this.http.get<Block>(
      `${this.base}/rest/blocks/${encodeURIComponent(formName)}/${encodeURIComponent(blockName)}`
    );
  }
}
