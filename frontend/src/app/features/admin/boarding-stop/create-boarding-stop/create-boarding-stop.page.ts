import { Component, inject, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { Router } from '@angular/router';
import { BoardingStopCreateRequest } from '../../../../shared/types/boarding-stop.type';
import { BoardingStopService } from '../boarding-stop.service';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';

@Component({
  selector: 'app-create-boarding-stop',
  imports: [TextFieldComponent, FormField, HeaderComponent],
  templateUrl: './create-boarding-stop.page.html',
})
export class CreateBoardingStop {
  router = inject(Router);

  boardingStop = signal<BoardingStopCreateRequest | null>(null);

  boardingStopService = inject(BoardingStopService);

  boardingStopFormModel = signal<BoardingStopCreateRequest>({
    local: '',
  });

  boardingStopForm = form(this.boardingStopFormModel, (schema) => {
    required(schema.local, { message: 'Local is required' });
  })

  onSubmit() {
    if (!this.boardingStopForm().valid()) {
      return;
    }

    this.boardingStopService.createBoardingStop(this.boardingStopFormModel())
    this.router.navigate(['/admin/boarding-stops']);
  }
}
