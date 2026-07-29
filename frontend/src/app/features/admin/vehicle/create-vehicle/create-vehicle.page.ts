import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { VehicleService } from '../vehicle.service';
import { VehicleCreateRequest } from '../../../../shared/types/vehicle.type';
import { form, FormField, required, min, minLength, maxLength } from '@angular/forms/signals';
import { BackButtonComponent } from '../../../../shared/components/buttons/back-button/back-button.component';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { NumberFieldComponent } from '../../../../shared/components/forms/number-field/number-field.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';

@Component({
  selector: 'app-create-vehicle',
  imports: [BackButtonComponent, HeaderComponent, TextFieldComponent, FormField, NumberFieldComponent],
  templateUrl: './create-vehicle.page.html',
})
export class CreateVehiclePage {
  router = inject(Router);

  vehicle = signal<VehicleCreateRequest | null>(null);

  vehicleService = inject(VehicleService);

  vehicleFormModel = signal<VehicleCreateRequest>({
    plate: '',
    capacity: 0,
  });

  vehicleForm = form(this.vehicleFormModel, (schema) => {
    required(schema.plate, { message: 'Plate is required' });
    minLength(schema.plate, 7, { message: 'Plate must be exactly 7 characters long' });
    maxLength(schema.plate, 7, { message: 'Plate must be exactly 7 characters long' });
    required(schema.capacity, { message: 'Capacity is required' });
    min(schema.capacity, 1, { message: 'Capacity must be at least 1' });
  })

  onSubmit() {
    if (!this.vehicleForm().valid()) {
      return;
    }

    this.vehicleService.createVehicle(this.vehicleFormModel());
    this.router.navigate(['/admin/vehicles']);
  }
}
