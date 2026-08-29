import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UI_COPY } from '../../../core/content/ui-copy';
import { DateLabelPipe } from '../../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../../shared/pipes/time-label.pipe';
import { Icon } from '../../../shared/components/icon/icon';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { HeaderComponent } from '../../../shared/components/list-header/header.component';
import { AdminOperation } from '../../../shared/types/admin-operation.type';
import { operationAttention, operationStage } from '../../../shared/utils/admin-operation';
import { isUpcomingDate, parseLocalDate } from '../../../shared/utils/date-time';
import { AdminOperationService } from '../operations/admin-operation.service';

@Component({
  selector: 'app-admin-home',
  imports: [DateLabelPipe, HeaderComponent, Icon, RouterLink, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './home.html',
})
export class AdminHomePage {
  private readonly operationService = inject(AdminOperationService);

  protected readonly copy = UI_COPY.admin.overview;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly operations = signal<AdminOperation[]>([]);

  protected readonly tripsToday = computed(() => {
    const today = this.dateKey(new Date());
    return this.operations().filter((operation) => operation.trip && operation.demand.referenceDate === today).length;
  });
  protected readonly openBookings = computed(() => this.operations()
    .filter((operation) => operation.demand.listStatus === 'OPEN')
    .reduce((total, operation) => total + operation.bookingCount, 0));
  protected readonly plannedSeats = computed(() => this.operations()
    .filter((operation) => operation.trip && isUpcomingDate(operation.demand.referenceDate))
    .reduce((total, operation) => total + (operation.trip?.vehicle?.capacity ?? 0), 0));
  protected readonly attentionOperations = computed(() => this.operations()
    .filter((operation) => operationAttention(operation).length > 0 && operation.trip?.status !== 'COMPLETED')
    .sort((a, b) => parseLocalDate(a.demand.referenceDate).getTime() - parseLocalDate(b.demand.referenceDate).getTime()));
  protected readonly nextSevenDays = computed(() => {
    const today = new Date();
    const start = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const limit = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7);
    return this.operations()
      .filter((operation) => {
        const date = parseLocalDate(operation.demand.referenceDate);
        return date.getTime() >= start.getTime() && date.getTime() < limit.getTime();
      })
      .sort((a, b) => parseLocalDate(a.demand.referenceDate).getTime() - parseLocalDate(b.demand.referenceDate).getTime());
  });

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.operationService.getOperations().subscribe({
      next: (operations) => this.operations.set(operations),
      error: () => {
        this.error.set('We could not load the operations overview. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected primaryAttention(operation: AdminOperation): string {
    return operationAttention(operation)[0]?.title ?? 'Review operation';
  }

  protected stage(operation: AdminOperation): string {
    return operationStage(operation);
  }

  private dateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
