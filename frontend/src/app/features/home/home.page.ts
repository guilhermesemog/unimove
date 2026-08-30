import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { UI_COPY } from '../../core/content/ui-copy';
import { BookingService } from '../admin/booking/booking.service';
import { InterestListService } from '../admin/interest-list/interest-list.service';
import { StudentService } from '../admin/student/student.service';
import { TripService } from '../admin/trip/trip.service';
import { Icon } from '../../shared/components/icon/icon';
import { JourneyCardComponent } from '../../shared/components/journey-card/journey-card.component';
import { RouteTimelineComponent } from '../../shared/components/route-timeline/route-timeline.component';
import { StatusChipComponent } from '../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../shared/pipes/time-label.pipe';
import { Booking } from '../../shared/types/booking.type';
import { DriverOperation } from '../../shared/types/driver-operation.type';
import { InterestList } from '../../shared/types/interest-list.type';
import { Student } from '../../shared/types/student.type';
import { Trip } from '../../shared/types/trip.type';
import { User, UserRole } from '../../shared/types/user.type';
import { isBookingOpen, isUpcomingDate, parseLocalDate } from '../../shared/utils/date-time';

const PAGE_REQUEST = { page: 0, size: 100, sortBy: 'id', sortDirection: 'desc' as const };

@Component({
  selector: 'app-home',
  imports: [DateLabelPipe, Icon, JourneyCardComponent, RouteTimelineComponent, RouterLink, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './home.html',
})
export class HomePage {
  private readonly authService = inject(AuthService);
  private readonly bookingService = inject(BookingService);
  private readonly interestListService = inject(InterestListService);
  private readonly studentService = inject(StudentService);
  private readonly tripService = inject(TripService);

  protected readonly copy = UI_COPY.student.home;
  protected readonly driverCopy = UI_COPY.driver.today;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly user = signal<User | null>(null);
  protected readonly student = signal<Student | null>(null);
  protected readonly bookings = signal<Booking[]>([]);
  protected readonly trips = signal<Trip[]>([]);
  protected readonly availableTrips = signal<InterestList[]>([]);
  protected readonly driverOperation = signal<DriverOperation | null>(null);

  protected readonly isStudent = computed(() => this.user()?.role === UserRole.Student);
  protected readonly isConductor = computed(() => this.user()?.role === UserRole.Conductor);
  protected readonly upcomingBookings = computed(() => this.bookings()
    .filter((booking) => isUpcomingDate(booking.interestList.referenceDate))
    .sort((a, b) => parseLocalDate(a.interestList.referenceDate).getTime() - parseLocalDate(b.interestList.referenceDate).getTime()));
  protected readonly nextBooking = computed(() => this.upcomingBookings()[0] ?? null);
  protected readonly nextTrip = computed(() => {
    const booking = this.nextBooking();
    return booking ? this.trips().find((trip) => trip.interestList.id === booking.interestList.id) ?? null : null;
  });
  protected readonly bookableCount = computed(() => {
    const booked = new Set(this.bookings().map((booking) => booking.interestList.id));
    return this.availableTrips().filter((trip) => isBookingOpen(trip) && !booked.has(trip.id)).length;
  });
  protected readonly upcomingDriverTrips = computed(() => this.trips()
    .filter((trip) => isUpcomingDate(trip.interestList.referenceDate) && trip.status !== 'COMPLETED')
    .sort((a, b) => a.interestList.referenceDate.localeCompare(b.interestList.referenceDate)
      || a.interestList.departureTime.localeCompare(b.interestList.departureTime)));
  protected readonly nextDriverTrip = computed(() => this.upcomingDriverTrips()[0] ?? null);
  protected readonly todayDriverTrips = computed(() => {
    const today = this.dateKey(new Date());
    return this.upcomingDriverTrips().filter((trip) => trip.interestList.referenceDate === today);
  });
  protected readonly remainingTodayTrips = computed(() => {
    const nextTripId = this.nextDriverTrip()?.id;
    return this.todayDriverTrips().filter((trip) => trip.id !== nextTripId);
  });

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.authService.identify().pipe(
      switchMap((user) => {
        this.user.set(user);
        if (user.role === UserRole.Conductor) {
          return this.tripService.getTripsByUser(PAGE_REQUEST).pipe(
            switchMap((trips) => {
              this.trips.set(trips.content);
              const nextTrip = this.nextDriverTrip();
              return nextTrip
                ? this.tripService.getDriverOperation(nextTrip.id).pipe(map((driverOperation) => ({ driverOperation })))
                : of({ driverOperation: null });
            }),
          );
        }
        if (user.role !== UserRole.Student) return of({ user });

        return forkJoin({
          student: this.studentService.getStudentById(user.id),
          bookings: this.bookingService.getUserBookings(PAGE_REQUEST),
          trips: this.tripService.getTripsByUser(PAGE_REQUEST),
          available: this.interestListService.getInterestLists({ ...PAGE_REQUEST, sortBy: 'referenceDate', sortDirection: 'asc' }),
        });
      }),
    ).subscribe({
      next: (result) => {
        if ('student' in result) {
          this.student.set(result.student);
          this.bookings.set(result.bookings.content);
          this.trips.set(result.trips.content);
          this.availableTrips.set(result.available.content);
        }
        if ('driverOperation' in result) this.driverOperation.set(result.driverOperation);
      },
      error: () => {
        this.error.set('We could not load your overview. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  private dateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
