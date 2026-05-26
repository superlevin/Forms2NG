import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { FormDetailComponent } from './form-detail.component';
import { FormsService } from '../../services/forms.service';

describe('FormDetailComponent', () => {
  let component: FormDetailComponent;
  let fixture: ComponentFixture<FormDetailComponent>;
  let mockFormsService: jasmine.SpyObj<FormsService>;

  beforeEach(async () => {
    mockFormsService = jasmine.createSpyObj('FormsService', [
      'getStats',
      'getBlocks',
      'getCanvases',
      'getTriggersForBlock',
      'getBlock'
    ]);

    mockFormsService.getStats.and.returnValue(of({
      numberOfBlocks: 1,
      numberOfTriggers: 2,
      numberOfItems: 3,
      numberOfCanvas: 1
    }));
    mockFormsService.getBlocks.and.returnValue(of([{ name: 'BLK1', numberOfTriggers: 2 }]));
    mockFormsService.getCanvases.and.returnValue(of([{ name: 'CVS1' }]));
    mockFormsService.getTriggersForBlock.and.returnValue(of([]));
    mockFormsService.getBlock.and.returnValue(of({ name: 'BLK1', databaseBlock: true }));

    await TestBed.configureTestingModule({
      imports: [FormDetailComponent],
      providers: [
        { provide: FormsService, useValue: mockFormsService },
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ formName: 'TESTFORM' })) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FormDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load form data on init', () => {
    expect(component.formName).toBe('TESTFORM');
    expect(component.blocks.length).toBe(1);
    expect(component.stats?.numberOfBlocks).toBe(1);
    expect(component.canvases.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should load triggers and block details when block selected', () => {
    const trigger = { name: 'WHEN-NEW-FORM-INSTANCE', triggerText: 'NULL;' };
    mockFormsService.getTriggersForBlock.and.returnValue(of([trigger]));
    mockFormsService.getBlock.and.returnValue(of({ name: 'BLK1', queryAllowed: true }));

    component.selectBlock({ name: 'BLK1' });

    expect(component.selectedBlock?.name).toBe('BLK1');
    expect(component.blockDetails?.name).toBe('BLK1');
    expect(component.triggers.length).toBe(1);
    expect(component.triggersLoading).toBeFalse();
    expect(component.blockLoading).toBeFalse();
  });
});
