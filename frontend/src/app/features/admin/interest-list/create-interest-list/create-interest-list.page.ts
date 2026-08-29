import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { form, FormField, required } from '@angular/forms/signals';
import { InterestListService } from '../interest-list.service';
import { InterestListCreateRequest } from '../../../../shared/types/interest-list.type';
import { DateFieldComponent } from '../../../../shared/components/forms/date-field/date-field.component';
import { HeaderComponent } from "../../../../shared/components/list-header/header.component";
import { TimeFieldComponent } from '../../../../shared/components/forms/time-field/time-field.component';
import { SelectFieldComponent, SelectOption } from "../../../../shared/components/forms/select-field/select-field.component";
import { UniversityService } from '../../university/university.service';
import { University } from '../../../../shared/types/university.type';

@Component({
  selector: 'app-create-interest-list.page',
  imports: [TimeFieldComponent, DateFieldComponent, FormField, HeaderComponent, SelectFieldComponent],
  templateUrl: './create-interest-list.page.html',
})
export class CreateInterestListPage {
  private router = inject(Router);
  interestListService = inject(InterestListService);
  interestList = signal<InterestListCreateRequest | null>(null);

  defaultReferenceDate = new Date().toISOString().split('T')[0];
  defaultClosingTime = '16:00';
  defaultDepartureTime = '17:30';
  defaultArrivalTime = '19:00';
  defaultReturnDepartureTime = '23:00';
  defaultReturnArrivalTime = '00:30';

  interestListFormModel = signal<InterestListCreateRequest>({
    referenceDate: this.defaultReferenceDate,
    closingTime: this.defaultClosingTime,
    departureTime: this.defaultDepartureTime,
    arrivalTime: this.defaultArrivalTime,
    returnDepartureTime: this.defaultReturnDepartureTime,
    returnArrivalTime: this.defaultReturnArrivalTime,
    destinationId: 0,
  });

  universityService = inject(UniversityService);
  universities = signal<Array<University>>([]);
  universityOptions = computed<SelectOption<number>[]>(() =>
    this.universities().map((u) => ({ value: u.id, label: u.name }))
  );

  fetchUniversities() {
    this.universityService.getUniversitiesAsList().subscribe((universities) => {
      this.universities.set(universities);
    });
  }

  interestListForm = form(this.interestListFormModel, (schema) => {
    required(schema.referenceDate, { message: 'Reference date is required' });
    required(schema.closingTime, { message: 'Closing time is required' });
    required(schema.departureTime, { message: 'Departure time is required' });
    required(schema.arrivalTime, { message: 'Arrival time is required' });
    required(schema.returnDepartureTime, { message: 'Return departure time is required' });
    required(schema.returnArrivalTime, { message: 'Return arrival time is required' });
    required(schema.destinationId, { message: 'Destination ID is required' });
  });

  ngOnInit() {
    this.fetchUniversities();
  }

  onSubmit() {
    if (!this.interestListForm().valid()) {
      return;
    }

    this.interestListService.createInterestList(this.interestListFormModel())
      .subscribe(() => {
        this.router.navigate(['/admin/interest-lists']);
      });
  }
}
