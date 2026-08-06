import { Component, inject, Input, signal } from '@angular/core';
import { InterestListService } from '../interest-list.service';
import { InterestList } from '../../../../shared/types/interest-list.type';
import { Booking } from '../../../../shared/types/booking.type';
import { BookingService } from '../../booking/booking.service';
import { CommonModule } from '@angular/common';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { StudentService } from '../../student/student.service';
import { BackButtonComponent } from '../../../../shared/components/buttons/back-button/back-button.component';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { ListState } from '../../../../shared/utils/list-state';
import { TripService } from '../../trip/trip.service';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';

@Component({
  selector: 'app-view-interest-list.page',
  imports: [CommonModule, HeaderComponent, BackButtonComponent, DataTableComponent, PaginationComponent, ConfirmDialogComponent],
  templateUrl: './view-interest-list.page.html',
})
export class ViewInterestListPage {
  @Input() id!: number;

  private interestListService = inject(InterestListService);
  private bookingService = inject(BookingService);
  private tripService = inject(TripService);

  interestList = signal<InterestList | null>(null);
  bookings = signal<Array<Booking>>([]);

  confirmDialog = new ConfirmDialogController();

  list = new ListState<Booking>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.bookingService.getBookingsByInterestListId(this.id, q),
    fetchByQuery: (term, q) => this.bookingService.getBookingsByInterestListId(this.id, q),
  });

  columns: TableColumn<Booking>[] = [
    { key: 'student', label: 'Student', sortable: true, format: (booking) => `${booking.student.user.firstName} ${booking.student.user.lastName}` },
    { key: 'bookingStatus', label: 'Status', sortable: true },
    { key: 'tripType', label: 'Type', sortable: true, format: (booking) => booking.tripType.replace('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase()) },
    { key: 'boardingLocation', label: 'Boarding Location', sortable: true, format: (booking) => booking.boardingLocation.local },
  ]


  ngOnInit() {
    this.list.fetch();
    this.fetchInterestList();
  }

  fetchInterestList() {
    this.interestListService.getInterestListById(this.id)
      .subscribe((interestList) => {
        this.interestList.set(interestList);
      });
  }

  onGenerateTrip() {
    if (!this.interestList()) {
      return;
    }

    this.confirmDialog.open({
      title: 'Generate Trip',
      message: `Are you sure you want to generate a trip for the interest list with reference date ${this.interestList()!.referenceDate}?`,
      confirmLabel: 'Generate',
      variant: 'default',
      action: () => this.generateTrip(),
    });
  }

  private generateTrip() {
    this.tripService.createTrip({
      interestListId: this.interestList()!.id
    }).subscribe(() => {
      this.list.fetch();
      this.fetchInterestList();
    });
  }
}
