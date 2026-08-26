import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UI_COPY } from '../../../core/content/ui-copy';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
})
export class PaginationComponent {
  protected readonly copy = UI_COPY;
  page = input.required<number>();
  totalPages = input.required<number>();
  maxVisiblePages = input(5);
  pageSize = input(10);
  pageSizeOptions = input<Array<number>>([10, 25, 50, 100]);

  pageChange = output<number>();
  pageSizeChange = output<number>();

  visiblePages = computed(() => {
    const total = this.totalPages();
    const current = this.page();
    const max = this.maxVisiblePages();

    if (total <= max) {
      return Array.from({ length: total }, (_, i) => i);
    }

    let start = Math.max(0, current - Math.floor(max / 2));
    let end = start + max;

    if (end > total) {
      end = total;
      start = end - max;
    }

    return Array.from({ length: end - start }, (_, i) => start + i);
  });

  isFirstPage = computed(() => this.page() === 0);
  isLastPage = computed(() => this.page() === this.totalPages() - 1);

  goToPage(newPage: number) {
    if (newPage < 0 || newPage > this.totalPages() - 1) {
      return;
    }
    this.pageChange.emit(newPage);
  }

  onPageSizeChange(event: Event) {
    const value = Number((event.target as HTMLSelectElement).value);
    this.pageSizeChange.emit(value);
  }
}
