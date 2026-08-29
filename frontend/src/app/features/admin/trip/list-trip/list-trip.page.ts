import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { UI_COPY } from '../../../../core/content/ui-copy';
import { AdminOperationCardComponent } from '../../../../shared/components/admin-operation-card/admin-operation-card.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { Icon } from '../../../../shared/components/icon/icon';
import { OccupancyMeterComponent } from '../../../../shared/components/occupancy-meter/occupancy-meter.component';
import { StatusChipComponent } from '../../../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../../../shared/pipes/time-label.pipe';
import { AdminOperation } from '../../../../shared/types/admin-operation.type';
import { operationAttention } from '../../../../shared/utils/admin-operation';
import { parseLocalDate } from '../../../../shared/utils/date-time';
import { AdminOperationService } from '../../operations/admin-operation.service';

@Component({
  selector: 'app-list-trip',
  imports: [AdminOperationCardComponent, DateLabelPipe, HeaderComponent, Icon, OccupancyMeterComponent, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './list-trip.page.html',
})
export class ListTripPage {
  private readonly operationService = inject(AdminOperationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly copy = UI_COPY.admin.schedule;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly operations = signal<AdminOperation[]>([]);
  protected readonly search = signal(this.route.snapshot.queryParamMap.get('search') ?? '');
  protected readonly status = signal(this.route.snapshot.queryParamMap.get('status') ?? 'ALL');
  protected readonly assignment = signal(this.route.snapshot.queryParamMap.get('assignment') ?? 'ALL');
  protected readonly dateFrom = signal(this.route.snapshot.queryParamMap.get('from') ?? '');
  protected readonly dateTo = signal(this.route.snapshot.queryParamMap.get('to') ?? '');
  protected readonly view = signal<'table' | 'calendar'>(this.route.snapshot.queryParamMap.get('view') === 'calendar' ? 'calendar' : 'table');

  protected readonly scheduledOperations = computed(() => this.operations()
    .filter((operation) => !!operation.trip)
    .filter((operation) => this.status() === 'ALL' || operation.trip?.status === this.status())
    .filter((operation) => {
      if (this.assignment() === 'UNASSIGNED') return !operation.trip?.conductor || !operation.trip?.vehicle;
      if (this.assignment() === 'ASSIGNED') return !!operation.trip?.conductor && !!operation.trip?.vehicle;
      return true;
    })
    .filter((operation) => !this.dateFrom() || operation.demand.referenceDate >= this.dateFrom())
    .filter((operation) => !this.dateTo() || operation.demand.referenceDate <= this.dateTo())
    .filter((operation) => {
      const term = this.search().trim().toLowerCase();
      if (!term) return true;
      const driver = `${operation.trip?.conductor?.user.firstName ?? ''} ${operation.trip?.conductor?.user.lastName ?? ''}`;
      return operation.demand.destination.name.toLowerCase().includes(term)
        || driver.toLowerCase().includes(term)
        || (operation.trip?.vehicle?.plate ?? '').toLowerCase().includes(term);
    })
    .sort((a, b) => parseLocalDate(a.demand.referenceDate).getTime() - parseLocalDate(b.demand.referenceDate).getTime()));
  protected readonly calendarGroups = computed(() => {
    const groups = new Map<string, AdminOperation[]>();
    for (const operation of this.scheduledOperations()) {
      const date = operation.demand.referenceDate;
      groups.set(date, [...(groups.get(date) ?? []), operation]);
    }
    return [...groups.entries()];
  });

  ngOnInit(): void { this.fetch(); }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.operationService.getOperations().subscribe({
      next: (operations) => this.operations.set(operations),
      error: () => { this.error.set('We could not load the trip schedule. Please try again.'); this.loading.set(false); },
      complete: () => this.loading.set(false),
    });
  }

  protected hasAttention(operation: AdminOperation): boolean { return operationAttention(operation).length > 0; }
  protected openOperation(operation: AdminOperation): void { this.router.navigate(['/admin/interest-lists', operation.demand.id, 'view']); }

  protected updateSearch(event: Event): void { this.search.set((event.target as HTMLInputElement).value); this.updateUrl(); }
  protected updateStatus(event: Event): void { this.status.set((event.target as HTMLSelectElement).value); this.updateUrl(); }
  protected updateAssignment(event: Event): void { this.assignment.set((event.target as HTMLSelectElement).value); this.updateUrl(); }
  protected updateFrom(event: Event): void { this.dateFrom.set((event.target as HTMLInputElement).value); this.updateUrl(); }
  protected updateTo(event: Event): void { this.dateTo.set((event.target as HTMLInputElement).value); this.updateUrl(); }
  protected setView(view: 'table' | 'calendar'): void { this.view.set(view); this.updateUrl(); }

  protected clearFilters(): void {
    this.search.set(''); this.status.set('ALL'); this.assignment.set('ALL'); this.dateFrom.set(''); this.dateTo.set(''); this.updateUrl();
  }

  private updateUrl(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: {
        search: this.search() || null,
        status: this.status() === 'ALL' ? null : this.status(),
        assignment: this.assignment() === 'ALL' ? null : this.assignment(),
        from: this.dateFrom() || null,
        to: this.dateTo() || null,
        view: this.view() === 'table' ? null : this.view(),
      },
    });
  }
}
