import { Component, input, output, contentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableColumn } from './table-column.type';
import { UiStateComponent } from '../ui-state/ui-state.component';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, UiStateComponent],
  templateUrl: './data-table.component.html',
})
export class DataTableComponent<T> {
  columns = input.required<TableColumn<T>[]>();
  data = input.required<T[]>();
  trackBy = input<(row: T) => string | number>((row: any) => row.id);
  sortBy = input<string | null>(null);
  sortDirection = input<'asc' | 'desc'>('asc');
  emptyMessage = input('No data available.');
  loading = input(false);
  error = input<string | null>(null);

  sortChange = output<string>();
  retry = output<void>();

  rowActions = contentChild<TemplateRef<{ $implicit: T }>>('rowActions');

  onSort(column: TableColumn<T>) {
    if (column.sortable) this.sortChange.emit(column.key);
  }

  getValue(row: T, column: TableColumn<T>): string {
    if (column.format) return column.format(row);
    return String(row[column.key] ?? '');
  }

  getColumnCount(): number {
    return this.columns().length + (this.rowActions() ? 1 : 0);
  }
}
