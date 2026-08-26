import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { UniversityService } from '../university.service';
import { UniversityCreateRequest } from '../../../../shared/types/university.type';
import { form, FormField, required } from '@angular/forms/signals';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';

@Component({
  selector: 'app-create-university',
  imports: [TextFieldComponent, FormField, HeaderComponent],
  templateUrl: './create-university.page.html',
})
export class CreateUniversityPage {
  router = inject(Router);

  university = signal<UniversityCreateRequest | null>(null);

  universityService = inject(UniversityService);

  universityFormModel = signal<UniversityCreateRequest>({
    name: '',
    address: '',
  });

  universityForm = form(this.universityFormModel, (schema) => {
    required(schema.name, { message: 'Name is required' });
    required(schema.address, { message: 'Address is required' });
  })

  onSubmit() {
    if (!this.universityForm().valid()) {
      return;
    }

    this.universityService.createUniversity(this.universityFormModel()).subscribe(() => {
      this.router.navigate(['/admin/universities']);
    });
  }

}
