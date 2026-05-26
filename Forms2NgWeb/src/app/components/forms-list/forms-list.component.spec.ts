import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsListComponent } from './forms-list.component';
import { FormsService } from '../../services/forms.service';

describe('FormsListComponent', () => {
  let component: FormsListComponent;
  let fixture: ComponentFixture<FormsListComponent>;
  let mockFormsService: jasmine.SpyObj<FormsService>;

  beforeEach(async () => {
    mockFormsService = jasmine.createSpyObj('FormsService', [
      'getFormsList',
      'getMenuModuleList',
      'getObjectLibraryList'
    ]);

    await TestBed.configureTestingModule({
      imports: [FormsListComponent],
      providers: [
        provideRouter([]),
        { provide: FormsService, useValue: mockFormsService }
      ]
    }).compileComponents();
  });

  it('should show module lists on success', () => {
    mockFormsService.getFormsList.and.returnValue(of([{ formsName: 'MYFORM' }]));
    mockFormsService.getMenuModuleList.and.returnValue(of([{ formsName: 'MAIN_MENU' }]));
    mockFormsService.getObjectLibraryList.and.returnValue(of([{ formsName: 'COMMON_LIB' }]));

    fixture = TestBed.createComponent(FormsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.formsList.length).toBe(1);
    expect(component.menuModules.length).toBe(1);
    expect(component.objectLibraries.length).toBe(1);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  it('should show error on failure', () => {
    mockFormsService.getFormsList.and.returnValue(throwError(() => new Error('network')));
    mockFormsService.getMenuModuleList.and.returnValue(of([]));
    mockFormsService.getObjectLibraryList.and.returnValue(of([]));

    fixture = TestBed.createComponent(FormsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.error).toBeTruthy();
    expect(component.loading).toBeFalse();
  });
});
