import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component, inject, Input, signal } from '@angular/core';
import { form, FormField, required, minLength, maxLength, min } from '@angular/forms/signals';

import { Vehicle, VehicleUpdateRequest } from '../../../../shared/types/vehicle.type';
import { VehicleService } from '../vehicle.service';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { NumberFieldComponent } from '../../../../shared/components/forms/number-field/number-field.component';

@Component({
  selector: 'app-edit-vehicle',
  imports: [CommonModule, HeaderComponent, FormField, TextFieldComponent, ConfirmDialogComponent, NumberFieldComponent],
  templateUrl: './edit-vehicle.page.html',
})
export class EditVehiclePage {
  confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

  router = inject(Router);

  vehicleService = inject(VehicleService);

  @Input() id!: number;

  vehicle = signal<Vehicle | null>(null);

  vehicleFormModel = signal({
    plate: this.vehicle()?.plate || '',
    capacity: this.vehicle()?.capacity || 0,
  });

  vehicleForm = form(this.vehicleFormModel, (schema) => {
    required(schema.plate, { message: 'Plate is required' });
    minLength(schema.plate, 7, { message: 'Plate must be exactly 7 character long' });
    maxLength(schema.plate, 7, { message: 'Plate must be exactly 7 character long' });
    required(schema.capacity, { message: 'Capacity is required' });
    min(schema.capacity, 1, { message: 'Capacity must be at least 1' });
  });

  fetchVehicle() {
    this.vehicleService.getVehicleById(this.id).subscribe((vehicle) => {
      this.vehicleFormModel.set({
        plate: vehicle.plate,
        capacity: vehicle.capacity,
      });
      this.vehicle.set(vehicle);
    });
  }

  ngOnInit() {
    this.fetchVehicle();
  }

  onSubmit() {
    if (!this.vehicleForm().valid()) {
      return;
    }

    this.confirmDialog.set({
      open: true,
      title: 'Confirm changes',
      message: 'Are you sure you want to save the changes?',
      confirmLabel: 'Save',
      variant: 'default',
      action: () => {
        const updatedVehicle: VehicleUpdateRequest = {
          ...this.vehicle(),
          plate: this.vehicleFormModel().plate,
          capacity: this.vehicleFormModel().capacity,
        };
        this.vehicleService.updateVehicle(this.id, updatedVehicle).subscribe(() => {
          this.fetchVehicle();
        });
      }
    });
  }

  onDeleteClick() {
    this.confirmDialog.set({
      open: true,
      title: 'Delete vehicle',
      message: `Are you sure you want to delete ${this.vehicle()?.plate}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteVehicle(this.vehicle()!),
    });
  }

  onConfirmDialogConfirm() {
    this.confirmDialog().action?.();
    this.closeConfirmDialog();
  }

  onConfirmDialogCancel() {
    this.closeConfirmDialog();
  }

  private closeConfirmDialog() {
    this.confirmDialog.set(CLOSED_DIALOG);
  }

  private deleteVehicle(vehicle: Vehicle) {
    this.vehicleService.deleteVehicle(vehicle.id).subscribe(() => {
      this.router.navigate(['/admin/vehicles']);
    });
  }
}
