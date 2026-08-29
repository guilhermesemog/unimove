import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, switchMap } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { UI_COPY } from '../../core/content/ui-copy';
import { BookingService } from '../admin/booking/booking.service';
import { InterestListService } from '../admin/interest-list/interest-list.service';
import { StudentService } from '../admin/student/student.service';
import { AvailableTripCardComponent } from '../../shared/components/available-trip-card/available-trip-card.component';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { Booking } from '../../shared/types/booking.type';
import { InterestList } from '../../shared/types/interest-list.type';
import { Student } from '../../shared/types/student.type';
import { isBookingOpen, parseLocalDate, relativeDateLabel } from '../../shared/utils/date-time';

const PAGE_REQUEST = { page: 0, size: 100, sortBy: 'referenceDate', sortDirection: 'asc' as const };

@Component({
  selector: 'app-interest-list-page',
  standalone: true,
  imports: [AvailableTripCardComponent, HeaderComponent, UiStateComponent],
  templateUrl: './interest-list.page.html',
})
export class InterestListPage {
  private readonly authService = inject(AuthService);
  private readonly bookingService = inject(BookingService);
  private readonly interestListService = inject(InterestListService);
  private readonly studentService = inject(StudentService);
  private readonly router = inject(Router);

  protected readonly copy = UI_COPY.student.availability;
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly student = signal<Student | null>(null);
  protected readonly trips = signal<InterestList[]>([]);
  protected readonly bookings = signal<Booking[]>([]);
  protected readonly selectedDate = signal<string>('all');
  protected readonly selectedUniversityId = signal<number | 'all'>('all');

  protected readonly bookedIds = computed(() => new Set(this.bookings().map((booking) => booking.interestList.id)));
  protected readonly universityOptions = computed(() => [...new Map(this.trips()
    .map((trip) => [trip.destination.id, trip.destination] as const)).values()]
    .sort((a, b) => a.name.localeCompare(b.name)));
  protected readonly tripsForUniversity = computed(() => this.trips()
    .filter((trip) => this.selectedUniversityId() === 'all' || trip.destination.id === this.selectedUniversityId()));
  protected readonly availableTrips = computed(() => this.tripsForUniversity()
    .filter((trip) => isBookingOpen(trip))
    .filter((trip) => this.selectedDate() === 'all' || trip.referenceDate === this.selectedDate())
    .sort((a, b) => parseLocalDate(a.referenceDate).getTime() - parseLocalDate(b.referenceDate).getTime()));
  protected readonly dateOptions = computed(() => [...new Set(this.tripsForUniversity()
    .filter((trip) => isBookingOpen(trip))
    .map((trip) => trip.referenceDate))]);
  protected readonly origin = computed(() => this.student()?.preferredBoardingStop?.local ?? 'Choose a boarding stop');

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      trips: this.interestListService.getInterestLists(PAGE_REQUEST),
      bookings: this.bookingService.getUserBookings({ ...PAGE_REQUEST, sortBy: 'id', sortDirection: 'desc' }),
      student: this.authService.identify().pipe(switchMap((user) => this.studentService.getStudentById(user.id))),
    }).subscribe({
      next: ({ trips, bookings, student }) => {
        this.trips.set(trips.content);
        this.bookings.set(bookings.content);
        this.student.set(student);
      },
      error: () => {
        this.error.set('We could not load the available trips. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected dateLabel(date: string): string {
    return relativeDateLabel(date);
  }

  protected selectUniversity(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedUniversityId.set(value === 'all' ? 'all' : Number(value));
    this.selectedDate.set('all');
  }

  protected openTrip(trip: InterestList): void {
    this.router.navigate(['/available-trips', trip.id]);
  }
}
