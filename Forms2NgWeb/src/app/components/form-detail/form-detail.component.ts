import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { FormsService } from '../../services/forms.service';
import { Block, Canvas, FormsStats, Trigger } from '../../models/forms.models';

/**
 * 顯示指定表單的統計資料、blocks、canvases，以及所選 block 的詳細內容。
 */
@Component({
  selector: 'app-form-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './form-detail.component.html',
  styleUrl: './form-detail.component.css'
})
export class FormDetailComponent implements OnInit {
  formName = '';
  stats: FormsStats | null = null;
  blocks: Block[] = [];
  canvases: Canvas[] = [];
  selectedBlock: Block | null = null;
  blockDetails: Block | null = null;
  triggers: Trigger[] = [];
  loading = true;
  blockLoading = false;
  triggersLoading = false;
  error: string | null = null;
  blockError: string | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly formsService: FormsService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.formName = params.get('formName') ?? '';
      this.loadData();
    });
  }

  /**
   * 根據目前路由中的表單名稱，載入摘要、block 與 canvas 資料。
   */
  loadData(): void {
    if (!this.formName) {
      this.error = 'No form name provided.';
      this.loading = false;
      return;
    }

    this.loading = true;
    this.error = null;
    this.selectedBlock = null;
    this.blockDetails = null;
    this.triggers = [];
    this.blockError = null;

    forkJoin({
      stats: this.formsService.getStats(this.formName),
      blocks: this.formsService.getBlocks(this.formName),
      canvases: this.formsService.getCanvases(this.formName)
    }).subscribe({
      next: ({ stats, blocks, canvases }) => {
        this.stats = stats;
        this.blocks = blocks;
        this.canvases = canvases;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load form data.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  /**
   * 切換目前選取的 block，並同步載入該 block 的詳細資料與 triggers。
   */
  selectBlock(block: Block): void {
    this.selectedBlock = block;
    this.blockDetails = null;
    this.triggers = [];
    this.blockError = null;
    this.blockLoading = true;
    this.triggersLoading = true;

    this.formsService.getBlock(this.formName, block.name).subscribe({
      next: (details) => {
        this.blockDetails = details;
        this.blockLoading = false;
      },
      error: (err) => {
        this.blockError = 'Failed to load block details.';
        this.blockLoading = false;
        console.error(err);
      }
    });

    this.formsService.getTriggersForBlock(this.formName, block.name).subscribe({
      next: (triggers) => {
        this.triggers = triggers;
        this.triggersLoading = false;
      },
      error: (err) => {
        this.blockError = this.blockError ?? 'Failed to load triggers.';
        this.triggersLoading = false;
        console.error(err);
      }
    });
  }
}
