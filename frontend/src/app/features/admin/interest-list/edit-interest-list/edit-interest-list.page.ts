import { Component, computed, inject, Input, input, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { BackButtonComponent } from '../../../../shared/components/buttons/back-button/back-button.component';
import { DateFieldComponent } from '../../../../shared/components/forms/date-field/date-field.component';
import { SelectFieldComponent, SelectOption } from '../../../../shared/components/forms/select-field/select-field.component';
import { TimeFieldComponent } from '../../../../shared/components/forms/time-field/time-field.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { Router } from '@angular/router';
import { InterestListService } from '../interest-list.service';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { InterestList, ListStatus } from '../../../../shared/types/interest-list.type';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { University } from '../../../../shared/types/university.type';
import { UniversityService } from '../../university/university.service';

@Component({
  selector: 'app-edit-interest-list.page',
  imports: [ConfirmDialogComponent, BackButtonComponent, TimeFieldComponent, DateFieldComponent, FormField, HeaderComponent, SelectFieldComponent, SelectFieldComponent],
  templateUrl: './edit-interest-list.page.html',
})
export class EditInterestListPage {
  @Input() id!: number;

  private router = inject(Router);
  private interestListService = inject(InterestListService);

  confirmDialog = new ConfirmDialogController();

  interestList = signal<InterestList | null>(null);
  interestListFormModel = signal({
    referenceDate: '',
    closingTime: '',
    departureTime: '',
    arrivalTime: '',
    returnDepartureTime: '',
    returnArrivalTime: '',
    destinationId: 0,
    listStatus: ListStatus.PROCESSING,
  });
  interestListForm = form(this.interestListFormModel, (schema) => {
    required(schema.referenceDate, { message: 'Reference date is required' });
    required(schema.closingTime, { message: 'Closing time is required' });
    required(schema.departureTime, { message: 'Departure time is required' });
    required(schema.arrivalTime, { message: 'Arrival time is required' });
    required(schema.returnDepartureTime, { message: 'Return departure time is required' });
    required(schema.returnArrivalTime, { message: 'Return arrival time is required' });
    required(schema.destinationId, { message: 'Destination ID is required' });
  });

  fetchInterestList() {
    this.interestListService.getInterestListById(this.id).subscribe((interestList) => {
      this.interestListFormModel.set({
        referenceDate: interestList.referenceDate,
        closingTime: interestList.closingTime,
        departureTime: interestList.departureTime,
        arrivalTime: interestList.arrivalTime,
        returnDepartureTime: interestList.returnDepartureTime,
        returnArrivalTime: interestList.returnArrivalTime,
        destinationId: interestList.destination.id,
        listStatus: interestList.listStatus,
      });
      this.interestList.set(interestList);
    });
  }

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

  statusOptions = computed<SelectOption<ListStatus>[]>(() => [
    { value: ListStatus.OPEN, label: 'Open' },
    { value: ListStatus.PROCESSING, label: 'Processing' },
    { value: ListStatus.CLOSED, label: 'Closed' },
  ]);

  ngOnInit() {
    this.fetchInterestList();
    this.fetchUniversities();
  }

  onSubmit() {
    if (!this.interestListForm().valid) {
      return;
    }

    this.confirmDialog.open({
      title: 'Confirm changes',
      message: 'Are you sure you want to save these changes?',
      confirmLabel: 'Save',
      variant: 'default',
      action: () => {
        this.interestListService.updateInterestList(this.id, {
          referenceDate: this.interestListFormModel().referenceDate,
          closingTime: this.interestListFormModel().closingTime,
          departureTime: this.interestListFormModel().departureTime,
          arrivalTime: this.interestListFormModel().arrivalTime,
          returnDepartureTime: this.interestListFormModel().returnDepartureTime,
          returnArrivalTime: this.interestListFormModel().returnArrivalTime,
          destinationId: this.interestListFormModel().destinationId,
          listStatus: this.interestListFormModel().listStatus,
        })
          .subscribe(() => {
            this.router.navigate(['/admin/interest-lists']);
          });
      }
    });
  }

  onDeleteClick() {
    this.confirmDialog.open({
      title: 'Delete interest list',
      message: `Are you sure you want to delete ${this.interestList()?.id}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteInterestList(this.interestList()!),
    });
  }

  private deleteInterestList(interestList: InterestList) {
    this.interestListService.deleteInterestList(interestList.id).subscribe(() => {
      this.router.navigate(['/admin/interest-lists']);
    });
  }

}
