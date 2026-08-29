import { Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { UI_COPY } from '../../core/content/ui-copy';
import { BookingService } from '../admin/booking/booking.service';
import { TripService } from '../admin/trip/trip.service';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { JourneyCardComponent } from '../../shared/components/journey-card/journey-card.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { Booking } from '../../shared/types/booking.type';
import { Trip } from '../../shared/types/trip.type';
import { isBookingOpen, isUpcomingDate, parseLocalDate } from '../../shared/utils/date-time';

const PAGE_REQUEST = { page: 0, size: 100, sortBy: 'id', sortDirection: 'desc' as const };

@Component({
  selector: 'app-bookings-page',
  imports: [ConfirmDialogComponent, HeaderComponent, JourneyCardComponent, UiStateComponent],
  templateUrl: './bookings.page.html',
})
export class BookingsPage {
  private readonly bookingService = inject(BookingService);
  private readonly tripService = inject(TripService);

  protected readonly copy = UI_COPY.student.journeys;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly feedback = signal<string | null>(null);
  protected readonly bookings = signal<Booking[]>([]);
  protected readonly trips = signal<Trip[]>([]);
  protected readonly activeTab = signal<'upcoming' | 'history'>('upcoming');
  protected readonly bookingToCancel = signal<Booking | null>(null);
  protected readonly cancelling = signal(false);

  protected readonly upcoming = computed(() => this.bookings()
    .filter((booking) => isUpcomingDate(booking.interestList.referenceDate) && this.tripFor(booking)?.status !== 'COMPLETED')
    .sort((a, b) => parseLocalDate(a.interestList.referenceDate).getTime() - parseLocalDate(b.interestList.referenceDate).getTime()));
  protected readonly history = computed(() => this.bookings()
    .filter((booking) => !isUpcomingDate(booking.interestList.referenceDate) || this.tripFor(booking)?.status === 'COMPLETED')
    .sort((a, b) => parseLocalDate(b.interestList.referenceDate).getTime() - parseLocalDate(a.interestList.referenceDate).getTime()));
  protected readonly displayedBookings = computed(() => this.activeTab() === 'upcoming' ? this.upcoming() : this.history());

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      bookings: this.bookingService.getUserBookings(PAGE_REQUEST),
      trips: this.tripService.getTripsByUser(PAGE_REQUEST),
    }).subscribe({
      next: ({ bookings, trips }) => {
        this.bookings.set(bookings.content);
        this.trips.set(trips.content);
      },
      error: () => {
        this.error.set('We could not load your trips. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected tripFor(booking: Booking): Trip | null {
    return this.trips().find((trip) => trip.interestList.id === booking.interestList.id) ?? null;
  }

  protected canCancel(booking: Booking): boolean {
    return isBookingOpen(booking.interestList);
  }

  protected confirmCancellation(): void {
    const booking = this.bookingToCancel();
    if (!booking || this.cancelling()) return;

    this.cancelling.set(true);
    this.bookingService.deleteBooking(booking.id).subscribe({
      next: () => {
        this.bookings.update((items) => items.filter((item) => item.id !== booking.id));
        this.feedback.set('Your booking has been cancelled.');
        this.bookingToCancel.set(null);
      },
      error: () => {
        this.error.set('We could not cancel this booking. Please try again.');
        this.bookingToCancel.set(null);
        this.cancelling.set(false);
      },
      complete: () => this.cancelling.set(false),
    });
  }
}
