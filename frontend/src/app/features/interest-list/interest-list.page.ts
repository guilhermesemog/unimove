import { Component, inject, signal } from '@angular/core';
import { InterestListService } from '../admin/interest-list/interest-list.service';
import { InterestList } from '../../shared/types/interest-list.type';
import { Router } from '@angular/router';
import { ListState } from '../../shared/utils/list-state';
import { CommonModule } from '@angular/common';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { DataTableComponent } from '../../shared/components/data-table/data-table.component';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';
import { BookingService } from '../admin/booking/booking.service';
import { TripType } from '../../shared/types/booking.type';

@Component({
  selector: 'app-interest-list.page',
  imports: [CommonModule, HeaderComponent, PaginationComponent],
  templateUrl: './interest-list.page.html',
})
export class InterestListPage {
  interestListService = inject(InterestListService);
  bookingService = inject(BookingService);

  interestLists = signal<Array<InterestList>>([]);
  router = inject(Router);

  list = new ListState<InterestList>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.interestListService.getInterestLists(q),
    fetchByQuery: (term, q) => this.interestListService.getInterestLists(q),
  });

  ngOnInit() {
    this.list.fetch();
  }

  createBooking(interestListId: number) {
    this.bookingService.createBooking({
      interestListId: interestListId,
      tripType: TripType.ROUND_TRIP,
    }).subscribe({
      next: () => {
        alert('Booking created successfully!');
      },
      error: (err) => {
        console.error('Error creating booking:', err);
        alert('Failed to create booking. Please try again.');
      }
    });
  }
}
