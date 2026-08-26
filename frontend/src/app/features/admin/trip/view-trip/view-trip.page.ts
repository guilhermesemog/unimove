import { Component, inject, Input, signal } from '@angular/core';
import { TripService } from '../trip.service';
import { InterestList } from '../../../../shared/types/interest-list.type';
import { CommonModule } from '@angular/common';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { ListState } from '../../../../shared/utils/list-state';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { Student } from '../../../../shared/types/student.type';
import { map } from 'rxjs/internal/operators/map';
import { Trip } from '../../../../shared/types/trip.type';

@Component({
  selector: 'app-view-trip.page',
  imports: [CommonModule, HeaderComponent, DataTableComponent, PaginationComponent, ConfirmDialogComponent],
  templateUrl: './view-trip.page.html',
})
export class ViewTripPage {
  @Input() id!: number;

  private tripService = inject(TripService);

  trip = signal<Trip | null>(null);
  students = signal<Array<Student>>([]);

  confirmDialog = new ConfirmDialogController();

  list = new ListState<Student>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.tripService.getStudentsByTripId(this.id, q),
    fetchByQuery: (term, q) => this.tripService.getStudentsByTripId(this.id, q),
  });

  columns: TableColumn<Student>[] = [
    { key: 'user', label: 'ID', sortable: true, format: (s) => s.user.id.toString() },
    { key: 'user', label: 'Student Name', sortable: true, format: (s) => `${s.user.firstName} ${s.user.lastName}` },
    { key: 'user', label: 'Phone', sortable: true, format: (s) => s.user.phone },
  ];

  studentTrackBy = (student: Student) => student.user.id;

  ngOnInit() {
    this.list.fetch();
    this.fetchTrip();
  }

  fetchTrip() {
    this.tripService.getTripById(this.id)
      .subscribe((trip) => {
        this.trip.set(trip);
      });
  }
}
