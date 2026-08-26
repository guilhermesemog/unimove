import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component, inject, Input, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';

import { University, UniversityUpdateRequest } from '../../../../shared/types/university.type';
import { UniversityService } from '../university.service';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';

@Component({
  selector: 'app-edit-university.page',
  imports: [CommonModule, FormField, TextFieldComponent, ConfirmDialogComponent, HeaderComponent],
  templateUrl: './edit-university.page.html',
})
export class EditUniversityPage {
  confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

  router = inject(Router);

  universityService = inject(UniversityService);

  @Input() id!: number;

  university = signal<University | null>(null);

  universityFormModel = signal({
    name: this.university()?.name || '',
    address: this.university()?.address || '',
  });

  universityForm = form(this.universityFormModel, (schema) => {
    required(schema.name, { message: 'Name is required' });
    required(schema.address, { message: 'Address is required' });
  });

  fetchUniversity() {
    this.universityService.getUniversityById(this.id).subscribe((university) => {
      this.universityFormModel.set({
        name: university.name,
        address: university.address,
      });
      this.university.set(university);
    });
  }

  ngOnInit() {
    this.fetchUniversity();
  }

  onSubmit() {
    if (!this.universityForm().valid()) {
      return;
    }

    this.confirmDialog.set({
      open: true,
      title: 'Confirm changes',
      message: `Are you sure you want to save changes for ${this.university()?.name}?`,
      confirmLabel: 'Save',
      variant: 'default',
      action: () => {
        const updatedUniversity: UniversityUpdateRequest = {
          ...this.university()!,
          name: this.universityFormModel().name,
          address: this.universityFormModel().address,
        };

        this.universityService.updateUniversity(this.id, updatedUniversity).subscribe(() => {
          this.fetchUniversity();
        });
      },
    });
  }

  onDeleteClick() {
    this.confirmDialog.set({
      open: true,
      title: 'Delete university',
      message: `Are you sure you want to delete ${this.university()?.name}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteUniversity(this.university()!),
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

  private deleteUniversity(university: University) {
    this.universityService.deleteUniversity(university.id).subscribe(() => {
      this.router.navigate(['/admin/universities']);
    });
  }

}
