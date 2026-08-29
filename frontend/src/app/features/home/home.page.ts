import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, switchMap } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { UI_COPY } from '../../core/content/ui-copy';
import { BookingService } from '../admin/booking/booking.service';
import { InterestListService } from '../admin/interest-list/interest-list.service';
import { StudentService } from '../admin/student/student.service';
import { TripService } from '../admin/trip/trip.service';
import { Icon } from '../../shared/components/icon/icon';
import { JourneyCardComponent } from '../../shared/components/journey-card/journey-card.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { Booking } from '../../shared/types/booking.type';
import { InterestList } from '../../shared/types/interest-list.type';
import { Student } from '../../shared/types/student.type';
import { Trip } from '../../shared/types/trip.type';
import { User, UserRole } from '../../shared/types/user.type';
import { isBookingOpen, isUpcomingDate, parseLocalDate } from '../../shared/utils/date-time';

const PAGE_REQUEST = { page: 0, size: 100, sortBy: 'id', sortDirection: 'desc' as const };

@Component({
  selector: 'app-home',
  imports: [Icon, JourneyCardComponent, RouterLink, UiStateComponent],
  templateUrl: './home.html',
})
export class HomePage {
  private readonly authService = inject(AuthService);
  private readonly bookingService = inject(BookingService);
  private readonly interestListService = inject(InterestListService);
  private readonly studentService = inject(StudentService);
  private readonly tripService = inject(TripService);

  protected readonly copy = UI_COPY.student.home;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly user = signal<User | null>(null);
  protected readonly student = signal<Student | null>(null);
  protected readonly bookings = signal<Booking[]>([]);
  protected readonly trips = signal<Trip[]>([]);
  protected readonly availableTrips = signal<InterestList[]>([]);

  protected readonly isStudent = computed(() => this.user()?.role === UserRole.Student);
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

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.authService.identify().pipe(
      switchMap((user) => {
        this.user.set(user);
        if (user.role !== UserRole.Student) return forkJoin({ user: [user] });

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
      },
      error: () => {
        this.error.set('We could not load your overview. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }
}
