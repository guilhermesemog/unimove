import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UI_COPY } from '../../core/content/ui-copy';
import { TelemetryService } from '../../core/telemetry/telemetry.service';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { Icon } from '../../shared/components/icon/icon';
import { RouteTimelineComponent } from '../../shared/components/route-timeline/route-timeline.component';
import { StatusChipComponent } from '../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../shared/pipes/time-label.pipe';
import { Trip } from '../../shared/types/trip.type';
import { isUpcomingDate } from '../../shared/utils/date-time';
import { TripService } from '../admin/trip/trip.service';

const SCHEDULE_REQUEST = { page: 0, size: 100, sortBy: 'interestList.referenceDate', sortDirection: 'asc' as const };

@Component({
  selector: 'app-trips-page',
  imports: [DateLabelPipe, HeaderComponent, Icon, RouteTimelineComponent, RouterLink, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './trips.page.html',
})
export class TripsPage {
  private readonly tripService = inject(TripService);
  private readonly telemetry = inject(TelemetryService);

  protected readonly copy = UI_COPY.driver.schedule;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly trips = signal<Trip[]>([]);
  protected readonly activeTab = signal<'upcoming' | 'history'>('upcoming');

  protected readonly upcoming = computed(() => this.trips()
    .filter((trip) => isUpcomingDate(trip.interestList.referenceDate) && trip.status !== 'COMPLETED')
    .sort(this.compareTrips));
  protected readonly history = computed(() => this.trips()
    .filter((trip) => !isUpcomingDate(trip.interestList.referenceDate) || trip.status === 'COMPLETED')
    .sort((a, b) => this.compareTrips(b, a)));
  protected readonly displayedTrips = computed(() => this.activeTab() === 'upcoming' ? this.upcoming() : this.history());
  protected readonly dateGroups = computed(() => {
    const groups = new Map<string, Trip[]>();
    for (const trip of this.displayedTrips()) {
      const date = trip.interestList.referenceDate;
      groups.set(date, [...(groups.get(date) ?? []), trip]);
    }
    return [...groups.entries()];
  });

  ngOnInit(): void {
    this.telemetry.startFlow('driver_operation');
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.tripService.getTripsByUser(SCHEDULE_REQUEST).subscribe({
      next: (response) => this.trips.set(response.content),
      error: () => {
        this.error.set('We could not load your schedule. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  private compareTrips(a: Trip, b: Trip): number {
    return a.interestList.referenceDate.localeCompare(b.interestList.referenceDate)
      || a.interestList.departureTime.localeCompare(b.interestList.departureTime);
  }
}
