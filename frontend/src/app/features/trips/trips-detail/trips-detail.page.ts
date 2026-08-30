import { Component, computed, inject, Input, signal } from '@angular/core';

import { UI_COPY } from '../../../core/content/ui-copy';
import { HeaderComponent } from '../../../shared/components/list-header/header.component';
import { Icon } from '../../../shared/components/icon/icon';
import { RouteTimelineComponent } from '../../../shared/components/route-timeline/route-timeline.component';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../../shared/pipes/date-label.pipe';
import { DriverManifestGroup, DriverManifestPassenger, DriverOperation } from '../../../shared/types/driver-operation.type';
import { TripType } from '../../../shared/types/booking.type';
import { TripService } from '../../admin/trip/trip.service';

@Component({
  selector: 'app-trips-detail-page',
  imports: [DateLabelPipe, HeaderComponent, Icon, RouteTimelineComponent, StatusChipComponent, UiStateComponent],
  templateUrl: './trips-detail.page.html',
})
export class TripsDetailPage {
  @Input() id!: number;

  private readonly tripService = inject(TripService);

  protected readonly copy = UI_COPY.driver.operation;
  protected readonly TripType = TripType;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly operation = signal<DriverOperation | null>(null);
  protected readonly activeLeg = signal<'outbound' | 'return'>('outbound');

  protected readonly legPassengers = computed(() => this.operation()?.passengers.filter((passenger) =>
    this.activeLeg() === 'outbound'
      ? passenger.tripType !== TripType.INBOUND
      : passenger.tripType !== TripType.OUTBOUND) ?? []);
  protected readonly outboundCount = computed(() => this.operation()?.passengers
    .filter((passenger) => passenger.tripType !== TripType.INBOUND).length ?? 0);
  protected readonly returnCount = computed(() => this.operation()?.passengers
    .filter((passenger) => passenger.tripType !== TripType.OUTBOUND).length ?? 0);
  protected readonly capacityExceeded = computed(() => {
    const capacity = this.operation()?.trip.vehicle?.capacity;
    return capacity !== undefined && capacity !== null && Math.max(this.outboundCount(), this.returnCount()) > capacity;
  });
  protected readonly manifestGroups = computed<DriverManifestGroup[]>(() => {
    const groups = new Map<number, DriverManifestGroup>();
    for (const passenger of this.legPassengers()) {
      const group = groups.get(passenger.boardingStop.id);
      if (group) group.passengers.push(passenger);
      else groups.set(passenger.boardingStop.id, { stop: passenger.boardingStop, passengers: [passenger] });
    }
    return [...groups.values()];
  });

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.tripService.getDriverOperation(Number(this.id)).subscribe({
      next: (operation) => this.operation.set(operation),
      error: () => {
        this.error.set('We could not load this operation. Confirm that it is assigned to you and try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected tripTypeLabel(passenger: DriverManifestPassenger): string {
    if (passenger.tripType === TripType.ROUND_TRIP) return 'Round Trip';
    return passenger.tripType === TripType.OUTBOUND ? 'Outbound Only' : 'Return Only';
  }
}
