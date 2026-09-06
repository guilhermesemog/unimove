import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TelemetryService } from '../../../../core/telemetry/telemetry.service';

import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { UiStateComponent } from '../../../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../../../shared/pipes/date-label.pipe';
import { RecurrencePlan, RecurrencePlanStatus } from '../../../../shared/types/recurrence-plan.type';
import { RecurrencePlanService } from '../recurrence-plan.service';

@Component({
  selector: 'app-list-recurrence-plan',
  imports: [HeaderComponent, PaginationComponent, UiStateComponent, DateLabelPipe],
  templateUrl: './list-recurrence-plan.page.html',
})
export class ListRecurrencePlanPage {
  private readonly service = inject(RecurrencePlanService);
  private readonly router = inject(Router);
  private readonly telemetry = inject(TelemetryService);

  protected readonly plans = signal<RecurrencePlan[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly feedback = signal<string | null>(null);
  protected readonly page = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly totalPages = signal(0);
  protected readonly busyId = signal<string | null>(null);

  ngOnInit(): void { this.fetch(); }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getAll({ page: this.page(), size: this.pageSize(), sortBy: 'createdAt', sortDirection: 'desc' })
      .subscribe({
        next: (response) => {
          this.plans.set(response.content);
          this.totalPages.set(response.page.totalPages);
        },
        error: () => { this.error.set('We could not load recurring plans.'); this.loading.set(false); },
        complete: () => this.loading.set(false),
      });
  }

  protected create(): void { this.router.navigate(['/admin/recurrence-plans/create']); }
  protected edit(plan: RecurrencePlan): void { this.router.navigate(['/admin/recurrence-plans', plan.id, 'edit']); }

  protected setStatus(plan: RecurrencePlan, status: RecurrencePlanStatus): void {
    this.run(plan.id, this.service.updateStatus(plan.id, status), `Plan ${status === 'ACTIVE' ? 'resumed' : 'paused'}.`);
  }

  protected generate(plan: RecurrencePlan): void {
    this.busyId.set(plan.id);
    this.service.generate(plan.id).subscribe({
      next: (result) => {
        this.feedback.set(`${result.createdCount} demands published, ${result.skippedCount} skipped, ${result.conflictCount} conflicts.`);
        this.telemetry.track('recurrence_published', { screen: 'recurrence_list', result: 'success' });
        if (result.conflictCount > 0)
          this.telemetry.track('recurrence_conflict_found', { screen: 'recurrence_list', result: 'conflict' });
        this.fetch();
      },
      error: () => { this.error.set('Generation failed. The batch was rolled back and can be retried.'); this.busyId.set(null); },
      complete: () => this.busyId.set(null),
    });
  }

  protected archive(plan: RecurrencePlan): void {
    if (!window.confirm(`Archive ${plan.name}? Published demands will be preserved.`)) return;
    this.run(plan.id, this.service.archive(plan.id), 'Plan archived.');
  }

  protected onPageChange(page: number): void { this.page.set(page); this.fetch(); }
  protected onPageSizeChange(size: number): void { this.pageSize.set(size); this.page.set(0); this.fetch(); }

  protected daySummary(plan: RecurrencePlan): string {
    return plan.daysOfWeek.map((day) => day.slice(0, 3)).join(', ');
  }

  private run(id: string, request: import('rxjs').Observable<unknown>, message: string): void {
    this.busyId.set(id);
    request.subscribe({
      next: () => { this.feedback.set(message); this.fetch(); },
      error: () => { this.error.set('We could not update this plan.'); this.busyId.set(null); },
      complete: () => this.busyId.set(null),
    });
  }
}
