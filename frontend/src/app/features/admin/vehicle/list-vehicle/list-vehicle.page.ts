import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { VehicleService } from '../vehicle.service';
import { Vehicle } from '../../../../shared/types/vehicle.type';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { AdminListToolbarComponent } from '../../../../shared/components/admin-list-toolbar/admin-list-toolbar.component';
import { ListState } from '../../../../shared/utils/list-state';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';

@Component({
  selector: 'app-list-vehicle',
  imports: [PaginationComponent, ConfirmDialogComponent, DataTableComponent, HeaderComponent, AdminListToolbarComponent],
  templateUrl: './list-vehicle.page.html',
})
export class ListVehiclePage {
  vehicleService = inject(VehicleService);
  router = inject(Router);

  list = new ListState<Vehicle>({
    initialSortBy: 'plate',
    fetchAll: (q) => this.vehicleService.getVehicles(q),
    fetchByQuery: (term, q) => this.vehicleService.getVehiclesByPlate(term, q),
  });

  confirmDialog = new ConfirmDialogController();

  columns: TableColumn<Vehicle>[] = [
    { key: 'plate', label: 'Plate', sortable: true },
    { key: 'capacity', label: 'Seats', sortable: true },
  ];

  ngOnInit() {
    this.list.fetch();
  }

  onEdit(vehicle: Vehicle) {
    this.router.navigate(['/admin/vehicles', vehicle.id, 'edit']);
  }

  onDeleteClick(vehicle: Vehicle) {
    this.confirmDialog.open({
      title: 'Delete vehicle',
      message: `Are you sure you want to delete ${vehicle.plate}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteVehicle(vehicle),
    });
  }

  onCreate() {
    this.router.navigate(['/admin/vehicles/create']);
  }

  private deleteVehicle(vehicle: Vehicle) {
    this.vehicleService.deleteVehicle(vehicle.id).subscribe(() => {
      this.list.fetch();
    });
  }
}
