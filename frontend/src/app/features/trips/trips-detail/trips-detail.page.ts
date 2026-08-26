import { Component, inject, Input, signal } from '@angular/core';

import { Trip } from '../../../shared/types/trip.type';
import { ListState } from '../../../shared/utils/list-state';
import { TripService } from '../../admin/trip/trip.service';
import { Student } from '../../../shared/types/student.type';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { HeaderComponent } from '../../../shared/components/list-header/header.component';
import { TableColumn } from '../../../shared/components/data-table/table-column.type';

@Component({
  selector: 'app-trips-detail.page',
  imports: [DataTableComponent, PaginationComponent, HeaderComponent],
  templateUrl: './trips-detail.page.html',
})
export class TripsDetailPage {

  @Input() id!: number;

  private tripService = inject(TripService);
  trip = signal<Trip | null>(null);

  list = new ListState<Student>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.tripService.getStudentsByTripId(this.id, q),
    fetchByQuery: (s, q) => this.tripService.getStudentsByTripId(this.id, q)
  }
  );

  columns: TableColumn<Student>[] = [
    { key: 'user', label: 'Name', sortable: true, format: (student) => `${student.user.firstName} ${student.user.lastName}` },
  ];

  ngOnInit() {
    this.list.fetch();
    this.tripService.getTripById(this.id).subscribe((trip) => {
      this.trip.set(trip);
    });
  }
}
