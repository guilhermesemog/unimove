import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { BookingService } from '../admin/booking/booking.service';
import { InterestListService } from '../admin/interest-list/interest-list.service';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { ListState } from '../../shared/utils/list-state';
import { BookingCreateRequest, TripType } from '../../shared/types/booking.type';
import { InterestList } from '../../shared/types/interest-list.type';
import { StatusChipComponent } from '../../shared/components/status-chip/status-chip.component';

@Component({
  selector: 'app-interest-list.page',
  standalone: true,
  imports: [CommonModule, HeaderComponent, PaginationComponent, StatusChipComponent],
  templateUrl: './interest-list.page.html',
})
export class InterestListPage {
  readonly TripType = TripType;

  interestListService = inject(InterestListService);
  bookingService = inject(BookingService);

  router = inject(Router);

  selectedInterestList = signal<InterestList | null>(null);
  selectedTripType = signal<TripType>(TripType.ROUND_TRIP);
  bookingLoading = signal(false);
  bookingSuccess = signal<string | null>(null);

  list = new ListState<InterestList>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.interestListService.getInterestLists(q),
    fetchByQuery: (term, q) => this.interestListService.getInterestLists(q),
  });

  ngOnInit() {
    this.list.fetch();
  }

  openDetails(interestList: InterestList) {
    this.selectedInterestList.set(interestList);
    this.selectedTripType.set(TripType.ROUND_TRIP);
    this.bookingSuccess.set(null);
  }

  closeDetails() {
    if (this.bookingLoading()) {
      return;
    }

    this.selectedInterestList.set(null);
    this.bookingSuccess.set(null);
  }

  updateTripType(tripType: TripType) {
    this.selectedTripType.set(tripType);
  }

  createBooking() {
    const interestList = this.selectedInterestList();

    if (!interestList || this.bookingLoading() || interestList.listStatus !== 'OPEN') {
      return;
    }

    const request: BookingCreateRequest = {
      interestListId: interestList.id,
      tripType: this.selectedTripType(),
    };

    this.bookingLoading.set(true);
    this.bookingSuccess.set(null);

    this.bookingService.createBooking(request).subscribe({
      next: () => {
        this.bookingSuccess.set('Your booking has been confirmed.');
      },
      complete: () => {
        this.bookingLoading.set(false);
      },
      error: (err) => {
        console.error('Error creating booking:', err);
        this.bookingLoading.set(false);
      }
    });
  }
}
