import { Component, computed, inject, Input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { form, FormField, minLength, maxLength, required, min } from '@angular/forms/signals';

import { User } from '../../../../shared/types/user.type';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { UserService } from '../../user/user.service';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { ConductorService } from '../conductor.service';
import { Conductor } from '../../../../shared/types/conductor.type';
import { DateFieldComponent } from '../../../../shared/components/forms/date-field/date-field.component';
import { CpfPipe } from '../../../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../../../shared/pipes/phone-pipe';
import { AccountSummaryComponent } from '../../../../shared/components/account-summary/account-summary.component';

@Component({
    selector: 'app-edit-conductor',
    imports: [FormField, TextFieldComponent, DateFieldComponent, ConfirmDialogComponent, HeaderComponent, CpfPipe, PhonePipe, AccountSummaryComponent],
    templateUrl: './edit-conductor.page.html',
})
export class EditConductorPage {
    confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

    conductorService = inject(ConductorService);
    userService = inject(UserService)

    router = inject(Router);

    @Input() id!: number;

    conductor = signal<Conductor | null>(null);

    conductorFormModel = signal({
        cpf: this.conductor()?.user.cpf || '',
        firstName: this.conductor()?.user.firstName || '',
        lastName: this.conductor()?.user.lastName || '',
        phone: this.conductor()?.user.phone || '',
        license: this.conductor()?.license || '',
        licenseExpirationDate: this.conductor()?.licenseExpirationDate || '',
    });

    conductorForm = form(this.conductorFormModel, (schema) => {
        required(schema.cpf, { message: 'CPF is required' });
        minLength(schema.cpf, 11, { message: 'CPF must be exactly 11 characters long' });
        maxLength(schema.cpf, 11, { message: 'CPF must be exactly 11 characters long' });
        required(schema.firstName, { message: 'First name is required' });
        required(schema.lastName, { message: 'Last name is required' });
        required(schema.phone, { message: 'Phone is required' });
        minLength(schema.phone, 8, { message: 'Phone must be at least 8 characters long' });
        maxLength(schema.phone, 12, { message: 'Phone must be at most 12 characters long' });
        required(schema.license, { message: 'License is required' });
        required(schema.licenseExpirationDate, { message: 'License expiration date is required' });
    });

    fetchConductor() {
        this.conductorService.getConductorById(this.id).subscribe((conductor) => {
            this.conductorFormModel.set({
                cpf: conductor.user.cpf,
                firstName: conductor.user.firstName,
                lastName: conductor.user.lastName,
                phone: conductor.user.phone,
                license: conductor.license,
                licenseExpirationDate: conductor.licenseExpirationDate,
            });
            this.conductor.set(conductor);
        });
    }

    ngOnInit() {
        this.fetchConductor();
    }

    onSubmit() {
        if (!this.conductorForm().valid()) {
            return;
        }

        this.confirmDialog.set({
            open: true,
            title: 'Confirm changes',
            message: `Are you sure you want to save changes for ${this.conductor()?.user.firstName} ${this.conductor()?.user.lastName}?`,
            confirmLabel: 'Save',
            variant: 'default',
            action: () => {
                const updatedConductor: Conductor = {
                    ...this.conductor(),
                    user: {
                        ...this.conductor()!.user,
                        cpf: this.conductorFormModel().cpf,
                        firstName: this.conductorFormModel().firstName,
                        lastName: this.conductorFormModel().lastName,
                        phone: this.conductorFormModel().phone,
                    },
                    license: this.conductorFormModel().license,
                    licenseExpirationDate: this.conductorFormModel().licenseExpirationDate,
                };

                this.conductorService.updateConductor(this.id, updatedConductor).subscribe(() => {
                    this.fetchConductor();
                });
            }
        });
    }

    onDeleteClick() {
        this.confirmDialog.set({
            open: true,
            title: 'Delete driver',
            message: `Are you sure you want to delete ${this.conductor()?.user.firstName} ${this.conductor()?.user.lastName}? This action cannot be undone.`,
            confirmLabel: 'Delete',
            variant: 'danger',
            action: () => this.deleteUser(this.conductor()?.user!),
        });
    }


    onToggleUserStatusClick() {
        this.confirmDialog.set({
            open: true,
            title: `${this.conductor()?.user.active ? 'Deactivate' : 'Activate'} driver`,
            message: `Are you sure you want to ${this.conductor()?.user.active ? 'deactivate' : 'activate'} ${this.conductor()?.user.firstName} ${this.conductor()?.user.lastName}? They will ${this.conductor()?.user.active ? 'lose' : 'gain'} access to the system.`,
            confirmLabel: `${this.conductor()?.user.active ? 'Deactivate' : 'Activate'}`,
            variant: this.conductor()?.user.active ? 'danger' : 'default',
            action: () => this.toggleUserStatus(this.conductor()?.user!),
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
            this.fetchConductor();
        });
    }
}
