import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, switchMap } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { UI_COPY } from '../../../core/content/ui-copy';
import { BoardingStopService } from '../../admin/boarding-stop/boarding-stop.service';
import { BookingService } from '../../admin/booking/booking.service';
import { InterestListService } from '../../admin/interest-list/interest-list.service';
import { StudentService } from '../../admin/student/student.service';
import { HeaderComponent } from '../../../shared/components/list-header/header.component';
import { Icon } from '../../../shared/components/icon/icon';
import { RouteTimelineComponent } from '../../../shared/components/route-timeline/route-timeline.component';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../../shared/pipes/time-label.pipe';
import { BoardingStop } from '../../../shared/types/boarding-stop.type';
import { Booking, BookingCreateRequest, TripType } from '../../../shared/types/booking.type';
import { InterestList } from '../../../shared/types/interest-list.type';
import { Student } from '../../../shared/types/student.type';
import { isBookingOpen } from '../../../shared/utils/date-time';

const PAGE_REQUEST = { page: 0, size: 100, sortBy: 'id', sortDirection: 'desc' as const };

@Component({
  selector: 'app-trip-checkout-page',
  standalone: true,
  imports: [DateLabelPipe, HeaderComponent, Icon, RouteTimelineComponent, RouterLink, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './trip-checkout.page.html',
})
export class TripCheckoutPage {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly boardingStopService = inject(BoardingStopService);
  private readonly bookingService = inject(BookingService);
  private readonly interestListService = inject(InterestListService);
  private readonly studentService = inject(StudentService);

  protected readonly copy = UI_COPY.student.checkout;
  protected readonly TripType = TripType;
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly success = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly trip = signal<InterestList | null>(null);
  protected readonly student = signal<Student | null>(null);
  protected readonly stops = signal<BoardingStop[]>([]);
  protected readonly bookings = signal<Booking[]>([]);
  protected readonly selectedTripType = signal(TripType.ROUND_TRIP);
  protected readonly selectedBoardingStopId = signal<number | null>(null);

  protected readonly alreadyBooked = computed(() => this.bookings().some((booking) => booking.interestList.id === this.trip()?.id));
  protected readonly selectedStop = computed(() => this.stops().find((stop) => stop.id === this.selectedBoardingStopId()) ?? null);
  protected readonly canBook = computed(() => {
    const trip = this.trip();
    return !!trip && isBookingOpen(trip) && !this.alreadyBooked() && this.selectedBoardingStopId() !== null;
  });

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.error.set('This trip could not be found.');
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      trip: this.interestListService.getInterestListById(id),
      student: this.authService.identify().pipe(switchMap((user) => this.studentService.getStudentById(user.id))),
      stops: this.boardingStopService.getBoardingStopsAsList(),
      bookings: this.bookingService.getUserBookings(PAGE_REQUEST),
    }).subscribe({
      next: ({ trip, student, stops, bookings }) => {
        this.trip.set(trip);
        this.student.set(student);
        this.stops.set(stops);
        this.bookings.set(bookings.content);
        this.selectedBoardingStopId.set(student.preferredBoardingStop?.id ?? stops[0]?.id ?? null);
      },
      error: () => {
        this.error.set('We could not load this trip. Please try again.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected selectBoardingStop(event: Event): void {
    this.selectedBoardingStopId.set(Number((event.target as HTMLSelectElement).value));
  }

  protected createBooking(): void {
    const trip = this.trip();
    const boardingStopId = this.selectedBoardingStopId();
    if (!trip || boardingStopId === null || !this.canBook() || this.submitting()) return;

    const request: BookingCreateRequest = {
      interestListId: trip.id,
      tripType: this.selectedTripType(),
      universityId: trip.destination.id,
      boardingStopId,
    };

    this.submitting.set(true);
    this.error.set(null);
    this.bookingService.createBooking(request).subscribe({
      next: () => this.success.set(true),
      error: () => {
        this.error.set('We could not confirm your booking. Please review the details and try again.');
        this.submitting.set(false);
      },
      complete: () => this.submitting.set(false),
    });
  }
}
