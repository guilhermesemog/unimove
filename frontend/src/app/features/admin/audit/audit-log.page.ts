import { DatePipe, KeyValuePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { HeaderComponent } from '../../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { AuditAction, AuditEvent, AuditEventDetail, AuditFilters } from '../../../shared/types/audit.type';
import { AuditEventService } from './audit.service';

@Component({
  selector: 'app-audit-log',
  imports: [DatePipe, FormsModule, HeaderComponent, KeyValuePipe, PaginationComponent, UiStateComponent],
  templateUrl: './audit-log.page.html',
})
export class AuditLogPage {
  private readonly service = inject(AuditEventService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly actions: AuditAction[] = [
    'DEMAND_PUBLISHED', 'DEMAND_UPDATED', 'DEMAND_STATUS_CHANGED', 'TRIP_CREATED',
    'TRIP_ASSIGNMENT_CHANGED', 'BOOKING_CREATED', 'BOOKING_REACTIVATED', 'BOOKING_CANCELLED',
    'USER_ACTIVATED', 'USER_DEACTIVATED', 'RECURRENCE_PLAN_CREATED', 'RECURRENCE_PLAN_UPDATED',
    'RECURRENCE_PLAN_STATUS_CHANGED', 'RECURRENCE_PLAN_ARCHIVED', 'RECURRENCE_PLAN_GENERATED',
  ];
  protected readonly entityTypes = ['InterestList', 'Trip', 'Booking', 'User', 'RecurrencePlan'];
  protected readonly events = signal<AuditEvent[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly detail = signal<AuditEventDetail | null>(null);
  protected readonly detailLoading = signal(false);
  protected readonly page = signal(0);
  protected readonly pageSize = signal(25);
  protected readonly totalPages = signal(0);

  protected fromDate = '';
  protected toDate = '';
  protected actor = '';
  protected action: AuditAction | '' = '';
  protected entityType = '';
  protected entityId = '';
  protected correlationId = '';

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.fromDate = params.get('from') ?? '';
    this.toDate = params.get('to') ?? '';
    this.actor = params.get('actor') ?? '';
    this.action = (params.get('action') as AuditAction | null) ?? '';
    this.entityType = params.get('entityType') ?? '';
    this.entityId = params.get('entityId') ?? '';
    this.correlationId = params.get('correlationId') ?? '';
    this.fetch();
  }

  protected applyFilters(): void {
    this.page.set(0);
    this.syncUrl();
    this.fetch();
  }

  protected clearFilters(): void {
    this.fromDate = ''; this.toDate = ''; this.actor = ''; this.action = '';
    this.entityType = ''; this.entityId = ''; this.correlationId = '';
    this.applyFilters();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getAll(this.filters(), this.page(), this.pageSize()).subscribe({
      next: (response) => {
        this.events.set(response.content);
        this.totalPages.set(response.page.totalPages);
        this.loading.set(false);
      },
      error: () => { this.error.set('We could not load the audit log. Check the filters and try again.'); this.loading.set(false); },
    });
  }

  protected openDetail(event: AuditEvent): void {
    this.detailLoading.set(true);
    this.detail.set(null);
    this.service.getById(event.id).subscribe({
      next: (detail) => { this.detail.set(detail); this.detailLoading.set(false); },
      error: () => { this.error.set('We could not load this audit event.'); this.detailLoading.set(false); },
    });
  }

  protected closeDetail(): void { this.detail.set(null); }
  protected onPageChange(page: number): void { this.page.set(page); this.fetch(); }
  protected onPageSizeChange(size: number): void { this.pageSize.set(size); this.page.set(0); this.fetch(); }
  protected formatAction(action: string): string {
    return action.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  private filters(): AuditFilters {
    return {
      from: this.fromDate ? new Date(`${this.fromDate}T00:00:00`).toISOString() : undefined,
      to: this.toDate ? new Date(new Date(`${this.toDate}T00:00:00`).getTime() + 86_400_000).toISOString() : undefined,
      actor: this.actor.trim() || undefined,
      action: this.action || undefined,
      entityType: this.entityType || undefined,
      entityId: this.entityId.trim() || undefined,
      correlationId: this.correlationId.trim() || undefined,
    };
  }

  private syncUrl(): void {
    this.router.navigate([], { relativeTo: this.route, replaceUrl: true, queryParams: {
      from: this.fromDate || null, to: this.toDate || null, actor: this.actor || null,
      action: this.action || null, entityType: this.entityType || null,
      entityId: this.entityId || null, correlationId: this.correlationId || null,
    }});
  }
}
