import { Routes } from '@angular/router';
import { FormsListComponent } from './components/forms-list/forms-list.component';
import { FormDetailComponent } from './components/form-detail/form-detail.component';

// 定義應用程式首頁、表單明細頁與未知路徑的導向規則。
export const routes: Routes = [
  { path: '', component: FormsListComponent },
  { path: 'form/:formName', component: FormDetailComponent },
  { path: '**', redirectTo: '' }
];
