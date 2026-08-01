import { CommonModule } from '@angular/common';
import { form, FormField, required } from '@angular/forms/signals';
import { Component, computed, inject, Input, signal } from '@angular/core';
import { BackButtonComponent } from '../../../../shared/components/buttons/back-button/back-button.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { SelectFieldComponent, SelectOption } from '../../../../shared/components/forms/select-field/select-field.component';
import { CLOSED_DIALOG, ConfirmDialogState } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { Router } from '@angular/router';
import { TripService } from '../trip.service';
import { Trip } from '../../../../shared/types/trip.type';
import { VehicleService } from '../../vehicle/vehicle.service';
import { ConductorService } from '../../conductor/conductor.service';
import { Conductor } from '../../../../shared/types/conductor.type';
import { Vehicle } from '../../../../shared/types/vehicle.type';

@Component({
  selector: 'app-edit-trip',
  imports: [CommonModule, FormField, TextFieldComponent, SelectFieldComponent, ConfirmDialogComponent, BackButtonComponent],
  templateUrl: './edit-trip.page.html',
})
export class EditTripPage {
  router = inject(Router);
  tripService = inject(TripService);
  confirmDialog = new ConfirmDialogController();
  @Input() id!: number;
  trip = signal<Trip | null>(null);

  conductorService = inject(ConductorService);
  conductors = signal<Array<Conductor>>([]);
  conductorOptions = computed<SelectOption<number>[]>(() =>
    this.conductors().map((c) => ({ value: c.user.id, label: `${c.user.firstName} ${c.user.lastName}` }))
  );

  vehicleService = inject(VehicleService);
  vehicles = signal<Array<Vehicle>>([]);
  vehicleOptions = computed<SelectOption<number>[]>(() =>
    this.vehicles().map((v) => ({ value: v.id, label: v.plate.toUpperCase() }))
  );

  tripFormModel = signal({
    conductorId: null as number | null,
    vehicleId: null as number | null,
  });

  tripForm = form(this.tripFormModel, (schema) => {
    schema.conductorId;
    schema.vehicleId;
  });

  fetchTrip() {
    this.tripService.getTripById(this.id).subscribe((trip) => {
      this.tripFormModel.set({
        conductorId: trip.conductor?.user.id ?? null,
        vehicleId: trip.vehicle?.id ?? null,
      });
      this.trip.set(trip);
    });
  }

  fetchConductors() {
    this.conductorService.getConductorsAsList().subscribe((conductors) => {
      this.conductors.set(conductors);
    });
  }

  fetchVehicles() {
    this.vehicleService.getVehiclesAsList().subscribe((vehicles) => {
      this.vehicles.set(vehicles);
    });
  }

  ngOnInit() {
    this.fetchTrip();
    this.fetchConductors();
    this.fetchVehicles();
  }

  onSubmit() {
    if (!this.tripForm().valid()) {
      return;
    }

    this.confirmDialog.open({
      title: 'Confirm changes',
      message: 'Are you sure you want to save these changes?',
      confirmLabel: 'Save',
      variant: 'default',
      action: () => {
        this.tripService.updateTrip(this.id, {
          conductorId: this.tripFormModel().conductorId!,
          vehicleId: this.tripFormModel().vehicleId!,
        }).subscribe((updatedTrip) => {
          this.trip.set(updatedTrip);
        });
      },
    });
  }

  onDeleteClick() {
    this.confirmDialog.open({
      title: 'Delete trip',
      message: `Are you sure you want to delete ${this.trip()?.id}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteTrip(this.trip()!),
    });
  }

  private deleteTrip(trip: Trip) {
    this.tripService.deleteTrip(trip.id).subscribe(() => {
      this.router.navigate(['/admin/trips']);
    });
  }
}