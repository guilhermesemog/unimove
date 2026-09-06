import { DatePipe } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';

import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { AuditEvent } from '../../../shared/types/audit.type';
import { AuditEventService } from './audit.service';

@Component({
  selector: 'app-audit-history',
  imports: [DatePipe, PaginationComponent, UiStateComponent],
  templateUrl: './audit-history.component.html',
})
export class AuditHistoryComponent {
  entityIds = input.required<string[]>();
  private readonly service = inject(AuditEventService);

  protected readonly events = signal<AuditEvent[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly page = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly totalPages = signal(0);

  ngOnInit(): void { this.fetch(); }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getAll({ entityIds: this.entityIds() }, this.page(), this.pageSize()).subscribe({
      next: (response) => {
        this.events.set(response.content);
        this.totalPages.set(response.page.totalPages);
        this.loading.set(false);
      },
      error: () => { this.error.set('We could not load this operation history.'); this.loading.set(false); },
    });
  }

  protected onPageChange(page: number): void { this.page.set(page); this.fetch(); }
  protected onPageSizeChange(size: number): void { this.pageSize.set(size); this.page.set(0); this.fetch(); }
}
