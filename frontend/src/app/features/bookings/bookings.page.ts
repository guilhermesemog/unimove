import { Component, inject, signal } from '@angular/core';
import { BookingService } from '../admin/booking/booking.service';
import { ListState } from '../../shared/utils/list-state';
import { Booking } from '../../shared/types/booking.type';
import { PageParameters } from '../../shared/types/page.type';
import { DataTableComponent } from '../../shared/components/data-table/data-table.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { Trip } from '../../shared/types/trip.type';
import { TripService } from '../admin/trip/trip.service';
import { CommonModule } from '@angular/common';
import { EnumPipe } from '../../shared/pipes/enum-pipe';
import { HeaderComponent } from '../../shared/components/list-header/header.component';

@Component({
  selector: 'app-bookings.page',
  imports: [PaginationComponent, CommonModule, EnumPipe, HeaderComponent],
  templateUrl: './bookings.page.html',
})
export class BookingsPage {

  private bookingService = inject(BookingService);
  private tripService = inject(TripService);

  window = signal<'Trips' | 'Bookings'>('Trips');

  bokingList = new ListState<Booking>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.bookingService.getUserBookings(q),
    fetchByQuery: (s, q) => this.bookingService.getUserBookings(q)
  }
  );

  tripList = new ListState<Trip>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.tripService.getTripsByUser(q),
    fetchByQuery: (s, q) => this.tripService.getTripsByUser(q)
  }
  );

  ngOnInit() {
    this.bokingList.fetch();
    this.tripList.fetch();
  }
}
