import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { FormsService } from '../../services/forms.service';
import { MenuEntry } from '../../models/forms.models';

@Component({
  selector: 'app-forms-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './forms-list.component.html',
  styleUrl: './forms-list.component.css'
})
export class FormsListComponent implements OnInit {
  formsList: MenuEntry[] = [];
  menuModules: MenuEntry[] = [];
  objectLibraries: MenuEntry[] = [];
  loading = true;
  error: string | null = null;

  constructor(private readonly formsService: FormsService) {}

  ngOnInit(): void {
    forkJoin({
      forms: this.formsService.getFormsList(),
      menuModules: this.formsService.getMenuModuleList(),
      objectLibraries: this.formsService.getObjectLibraryList()
    }).subscribe({
      next: ({ forms, menuModules, objectLibraries }) => {
        this.formsList = forms;
        this.menuModules = menuModules;
        this.objectLibraries = objectLibraries;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load modules. Is the backend running?';
        this.loading = false;
        console.error(err);
      }
    });
  }
}
