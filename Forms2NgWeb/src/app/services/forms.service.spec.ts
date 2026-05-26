import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsService } from './forms.service';
import { environment } from '../../environments/environment';

describe('FormsService', () => {
  let service: FormsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FormsService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(FormsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should get forms list', () => {
    const mockData = [{ formsName: 'MYFORM' }];
    service.getFormsList().subscribe((data) => {
      expect(data.length).toBe(1);
      expect(data[0].formsName).toBe('MYFORM');
    });
    const req = httpMock.expectOne(`${environment.apiBase}/rest/menu/`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should get stats for a form', () => {
    const mockStats = { numberOfBlocks: 2, numberOfTriggers: 3, numberOfItems: 5, numberOfCanvas: 1 };
    service.getStats('MYFORM').subscribe((stats) => {
      expect(stats.numberOfBlocks).toBe(2);
    });
    const req = httpMock.expectOne(`${environment.apiBase}/rest/forms/stats/MYFORM`);
    req.flush(mockStats);
  });

  it('should get blocks for a form', () => {
    const mockBlocks = [{ name: 'BLK1', numberOfTriggers: 2 }];
    service.getBlocks('MYFORM').subscribe((blocks) => {
      expect(blocks.length).toBe(1);
    });
    const req = httpMock.expectOne(`${environment.apiBase}/rest/forms/blocks/MYFORM`);
    req.flush(mockBlocks);
  });

  it('should get triggers for a block', () => {
    const mockTriggers = [{ name: 'WHEN-NEW-FORM-INSTANCE', triggerText: 'NULL;' }];
    service.getTriggersForBlock('MYFORM', 'BLK1').subscribe((triggers) => {
      expect(triggers.length).toBe(1);
    });
    const req = httpMock.expectOne(`${environment.apiBase}/rest/forms/triggersforblock/MYFORM/BLK1`);
    req.flush(mockTriggers);
  });

  it('should get block details for a block', () => {
    service.getBlock('MYFORM', 'BLK1').subscribe((block) => {
      expect(block.name).toBe('BLK1');
    });
    const req = httpMock.expectOne(`${environment.apiBase}/rest/blocks/MYFORM/BLK1`);
    req.flush({ name: 'BLK1' });
  });

  it('should get canvases for a form', () => {
    service.getCanvases('MYFORM').subscribe((canvases) => {
      expect(canvases.length).toBe(1);
    });
    const req = httpMock.expectOne(`${environment.apiBase}/rest/forms/canvas/MYFORM`);
    req.flush([{ name: 'CVS1' }]);
  });

  it('should get MMB list', () => {
    service.getMenuModuleList().subscribe();
    const req = httpMock.expectOne(`${environment.apiBase}/rest/menu/mmb`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should get OLB list', () => {
    service.getObjectLibraryList().subscribe();
    const req = httpMock.expectOne(`${environment.apiBase}/rest/menu/olb`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
