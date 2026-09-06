import { Component, inject, Input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { form, FormField, minLength, maxLength, required } from '@angular/forms/signals';

import { User } from '../../../../shared/types/user.type';
import { UserService } from '../user.service';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { AuthService } from '../../../../core/auth/auth.service';
import { PhonePipe } from '../../../../shared/pipes/phone-pipe';
import { CpfPipe } from '../../../../shared/pipes/cpf-pipe';
import { AccountSummaryComponent } from '../../../../shared/components/account-summary/account-summary.component';

@Component({
  selector: 'app-edit-user',
  imports: [FormField, TextFieldComponent, ConfirmDialogComponent, HeaderComponent, PhonePipe, CpfPipe, AccountSummaryComponent],
  templateUrl: './edit-user.page.html',
})
export class EditUserPage {
  confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

  userService = inject(UserService);
  authService = inject(AuthService);
  router = inject(Router);

  @Input() id!: string;

  user = signal<User | null>(null);

  userFormModel = signal({
    cpf: this.user()?.cpf || '',
    firstName: this.user()?.firstName || '',
    lastName: this.user()?.lastName || '',
    phone: this.user()?.phone || '',
  });

  userForm = form(this.userFormModel, (schema) => {
    required(schema.cpf, { message: 'CPF is required' });
    minLength(schema.cpf, 11, { message: 'CPF must be exactly 11 characters long' });
    maxLength(schema.cpf, 11, { message: 'CPF must be exactly 11 characters long' });
    required(schema.firstName, { message: 'First name is required' });
    required(schema.lastName, { message: 'Last name is required' });
    required(schema.phone, { message: 'Phone is required' });
    minLength(schema.phone, 8, { message: 'Phone must be at least 8 characters long' });
    maxLength(schema.phone, 12, { message: 'Phone must be at most 12 characters long' });
  });


  fetchUser() {
    this.userService.getUserById(this.id).subscribe((user) => {
      this.userFormModel.set({
        cpf: user.cpf,
        firstName: user.firstName,
        lastName: user.lastName,
        phone: user.phone,
      });
      this.user.set(user);
    });
  }

  ngOnInit() {
    this.fetchUser();
  }

  onSubmit() {
    if (!this.userForm().valid()) {
      return;
    }

    this.confirmDialog.set({
      open: true,
      title: 'Confirm changes',
      message: `Are you sure you want to save changes for ${this.user()?.firstName} ${this.user()?.lastName}?`,
      confirmLabel: 'Save',
      variant: 'default',
      action: () => {
        const updatedUser: User = {
          ...this.user()!,
          cpf: this.userFormModel().cpf,
          firstName: this.userFormModel().firstName,
          lastName: this.userFormModel().lastName,
          phone: this.userFormModel().phone,
        };

        this.userService.updateUser(this.id, updatedUser).subscribe(() => {
          this.fetchUser();
        });
      },
    });
  }

  onDeleteClick() {
    this.confirmDialog.set({
      open: true,
      title: 'Delete user',
      message: `Are you sure you want to delete ${this.user()?.firstName} ${this.user()?.lastName}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteUser(this.user()!),
    });
  }

  onToggleUserStatusClick() {
    this.confirmDialog.set({
      open: true,
      title: `${this.user()?.active ? 'Deactivate' : 'Activate'} user`,
      message: `Are you sure you want to ${this.user()?.active ? 'deactivate' : 'activate'} ${this.user()?.firstName} ${this.user()?.lastName}? They will ${this.user()?.active ? 'lose' : 'gain'} access to the system.`,
      confirmLabel: `${this.user()?.active ? 'Deactivate' : 'Activate'}`,
      variant: this.user()?.active ? 'danger' : 'default',
      action: () => this.toggleUserStatus(this.user()!),
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


  private deleteUser(user: User) {
    this.userService.deleteUser(user.id).subscribe(() => {
      this.router.navigate(['/admin/users']);
    });
  }

  private toggleUserStatus(user: User) {
    this.userService.toggleUserActiveStatus(user.id).subscribe(() => {
      this.fetchUser();
    });
  }

}
