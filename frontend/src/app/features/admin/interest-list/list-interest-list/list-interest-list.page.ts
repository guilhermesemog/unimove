import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { UI_COPY } from '../../../../core/content/ui-copy';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { Icon } from '../../../../shared/components/icon/icon';
import { OccupancyMeterComponent } from '../../../../shared/components/occupancy-meter/occupancy-meter.component';
import { StatusChipComponent } from '../../../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../../../shared/pipes/time-label.pipe';
import { AdminOperation } from '../../../../shared/types/admin-operation.type';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { parseLocalDate } from '../../../../shared/utils/date-time';
import { AdminOperationService } from '../../operations/admin-operation.service';
import { InterestListService } from '../interest-list.service';

@Component({
  selector: 'app-list-interest-list-page',
  imports: [ConfirmDialogComponent, DateLabelPipe, HeaderComponent, Icon, OccupancyMeterComponent, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './list-interest-list.page.html',
})
export class ListInterestListPage {
  private readonly operationService = inject(AdminOperationService);
  private readonly interestListService = inject(InterestListService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly copy = UI_COPY.admin.demand;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly operations = signal<AdminOperation[]>([]);
  protected readonly destinationId = signal(this.route.snapshot.queryParamMap.get('destination'));
  protected readonly status = signal(this.route.snapshot.queryParamMap.get('status') ?? 'ALL');
  protected readonly dateFrom = signal(this.route.snapshot.queryParamMap.get('from') ?? '');
  protected readonly dateTo = signal(this.route.snapshot.queryParamMap.get('to') ?? '');
  protected readonly confirmDialog = new ConfirmDialogController();

  protected readonly destinations = computed(() => [...new Map(this.operations()
    .map((operation) => [operation.demand.destination.id, operation.demand.destination] as const)).values()]
    .sort((a, b) => a.name.localeCompare(b.name)));
  protected readonly filteredOperations = computed(() => this.operations()
    .filter((operation) => this.destinationId() === null || operation.demand.destination.id === this.destinationId())
    .filter((operation) => this.status() === 'ALL' || operation.demand.listStatus === this.status())
    .filter((operation) => !this.dateFrom() || operation.demand.referenceDate >= this.dateFrom())
    .filter((operation) => !this.dateTo() || operation.demand.referenceDate <= this.dateTo())
    .sort((a, b) => parseLocalDate(a.demand.referenceDate).getTime() - parseLocalDate(b.demand.referenceDate).getTime()));

  ngOnInit(): void { this.fetch(); }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.operationService.getOperations().subscribe({
      next: (operations) => this.operations.set(operations),
      error: () => { this.error.set('We could not load demand. Please try again.'); this.loading.set(false); },
      complete: () => this.loading.set(false),
    });
  }

  protected selectDestination(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.destinationId.set(value || null);
    this.updateUrl();
  }

  protected selectStatus(event: Event): void { this.status.set((event.target as HTMLSelectElement).value); this.updateUrl(); }
  protected setDateFrom(event: Event): void { this.dateFrom.set((event.target as HTMLInputElement).value); this.updateUrl(); }
  protected setDateTo(event: Event): void { this.dateTo.set((event.target as HTMLInputElement).value); this.updateUrl(); }

  protected clearFilters(): void {
    this.destinationId.set(null);
    this.status.set('ALL');
    this.dateFrom.set('');
    this.dateTo.set('');
    this.updateUrl();
  }

  protected openOperation(operation: AdminOperation): void { this.router.navigate(['/admin/interest-lists', operation.demand.id, 'view']); }
  protected editDemand(operation: AdminOperation): void { this.router.navigate(['/admin/interest-lists', operation.demand.id, 'edit']); }
  protected createDemand(): void { this.router.navigate(['/admin/interest-lists/create']); }

  protected deleteDemand(operation: AdminOperation): void {
    this.confirmDialog.open({
      title: 'Delete Demand',
      message: `Delete demand for ${operation.demand.destination.name} on ${operation.demand.referenceDate}? This action cannot be undone.`,
      confirmLabel: 'Delete Demand',
      variant: 'danger',
      action: () => this.interestListService.deleteInterestList(operation.demand.id).subscribe(() => this.fetch()),
    });
  }

  private updateUrl(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: {
        destination: this.destinationId(),
        status: this.status() === 'ALL' ? null : this.status(),
        from: this.dateFrom() || null,
        to: this.dateTo() || null,
      },
    });
  }

}
