import { Component, inject } from '@angular/core';

import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { SearchBarComponent } from '../../../../shared/components/search-bar/search-bar.component';
import { TripService } from '../trip.service';
import { ListState } from '../../../../shared/utils/list-state';
import { Trip } from '../../../../shared/types/trip.type';
import { Router } from '@angular/router';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';

@Component({
  selector: 'app-list-trip',
  imports: [PaginationComponent, ConfirmDialogComponent, DataTableComponent, HeaderComponent, SearchBarComponent, HeaderComponent],
  templateUrl: './list-trip.page.html',
})
export class ListTripPage {
  tripService = inject(TripService);
  router = inject(Router);

  list = new ListState<Trip>({
    initialSortBy: 'id',
    fetchAll: (q) => this.tripService.getTrips(q),
    fetchByQuery: (term, q) => this.tripService.getTripByConductorOrVehicle(term, q),
  });

  confirmDialog = new ConfirmDialogController();

  columns: TableColumn<Trip>[] = [
    { key: 'id' as keyof Trip, label: 'ID', sortable: true },
    { key: 'status', label: 'Status', sortable: true },
    { key: 'conductor', label: 'Conductor', format: (trip) => trip.conductor ? `${trip.conductor.user.firstName} ${trip.conductor.user.lastName}` : '-' },
    { key: 'vehicle', label: 'Vehicle', format: (trip) => trip.vehicle ? `${trip.vehicle.plate.toUpperCase()} (${trip.vehicle.capacity})` : '-' },
  ];

  ngOnInit() {
    this.list.fetch();
  }

  onEdit(trip: Trip) {
    this.router.navigate(['/admin/trips', trip.id, 'edit']);
  }

  onDeleteClick(trip: Trip) {
    this.confirmDialog.open({
      title: 'Delete trip',
      message: `Are you sure you want to delete ${trip.id}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteTrip(trip),
    });
  }

  onCreate() {
    this.router.navigate(['/admin/trips/create']);
  }

  private deleteTrip(trip: Trip) {
    this.tripService.deleteTrip(trip.id).subscribe(() => {
      this.list.fetch();
    });
  }

}
