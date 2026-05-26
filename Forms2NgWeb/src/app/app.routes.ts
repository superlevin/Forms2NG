import { Routes } from '@angular/router';
import { FormsListComponent } from './components/forms-list/forms-list.component';
import { FormDetailComponent } from './components/form-detail/form-detail.component';

export const routes: Routes = [
  { path: '', component: FormsListComponent },
  { path: 'form/:formName', component: FormDetailComponent },
  { path: '**', redirectTo: '' }
];
