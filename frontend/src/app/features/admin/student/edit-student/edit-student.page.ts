import { Component, computed, inject, Input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { form, FormField, minLength, maxLength, required, min } from '@angular/forms/signals';

import { User } from '../../../../shared/types/user.type';
import { StudentService } from '../student.service';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { UserService } from '../../user/user.service';
import { Student, StudentUpdateRequest } from '../../../../shared/types/student.type';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { NumberFieldComponent } from '../../../../shared/components/forms/number-field/number-field.component';
import { SelectFieldComponent, SelectOption } from "../../../../shared/components/forms/select-field/select-field.component";
import { UniversityService } from '../../university/university.service';
import { University } from '../../../../shared/types/university.type';
import { CpfPipe } from '../../../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../../../shared/pipes/phone-pipe';

@Component({
    selector: 'app-edit-student',
    imports: [CommonModule, FormField, TextFieldComponent, NumberFieldComponent, SelectFieldComponent, ConfirmDialogComponent, HeaderComponent, CpfPipe, PhonePipe],
    templateUrl: './edit-student.page.html',
})
export class EditStudentPage {
    confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

    studentService = inject(StudentService);
    userService = inject(UserService);
    universityService = inject(UniversityService);

    router = inject(Router);

    @Input() id!: number;

    student = signal<Student | null>(null);
    universities = signal<Array<University>>([]);
    universityOptions = computed<SelectOption<number>[]>(() =>
        this.universities().map((u) => ({ value: u.id, label: u.name }))
    );

    studentFormModel = signal({
        cpf: this.student()?.user.cpf || '',
        firstName: this.student()?.user.firstName || '',
        lastName: this.student()?.user.lastName || '',
        phone: this.student()?.user.phone || '',
        period: this.student()?.period || 0,
        course: this.student()?.course || '',
        address: this.student()?.address || '',
        universityId: this.student()?.university?.id ?? null,
    });

    studentForm = form(this.studentFormModel, (schema) => {
        required(schema.cpf, { message: 'CPF is required' });
        minLength(schema.cpf, 11, { message: 'CPF must be exactly 11 characters long' });
        maxLength(schema.cpf, 11, { message: 'CPF must be exactly 11 characters long' });
        required(schema.firstName, { message: 'First name is required' });
        required(schema.lastName, { message: 'Last name is required' });
        required(schema.phone, { message: 'Phone is required' });
        minLength(schema.phone, 8, { message: 'Phone must be at least 8 characters long' });
        maxLength(schema.phone, 12, { message: 'Phone must be at most 12 characters long' });
        required(schema.period, { message: 'Period is required' });
        min(schema.period, 1, { message: 'Period must be at least 1' });
        required(schema.course, { message: 'Course is required' });
        required(schema.address, { message: 'Address is required' });
        required(schema.universityId, { message: 'University is required' });
    });

    fetchStudent() {
        this.studentService.getStudentById(this.id).subscribe((student) => {
            this.studentFormModel.set({
                cpf: student.user.cpf,
                firstName: student.user.firstName,
                lastName: student.user.lastName,
                phone: student.user.phone,
                period: student.period,
                course: student.course,
                address: student.address,
                universityId: student.university.id,
            });
            this.student.set(student);
        });
    }

    fetchUniversities() {
        this.universityService.getUniversitiesAsList().subscribe((response) => {
            this.universities.set(response);
        });
    }

    ngOnInit() {
        this.fetchStudent();
        this.fetchUniversities();
    }

    onSubmit() {
        if (!this.studentForm().valid()) {
            return;
        }

        this.confirmDialog.set({
            open: true,
            title: 'Confirm changes',
            message: `Are you sure you want to save changes for ${this.student()?.user.firstName} ${this.student()?.user.lastName}?`,
            confirmLabel: 'Save',
            variant: 'default',
            action: () => {
                const updatedStudent: StudentUpdateRequest = {
                    ...this.student()!,
                    user: {
                        ...this.student()!.user,
                        cpf: this.studentFormModel().cpf,
                        firstName: this.studentFormModel().firstName,
                        lastName: this.studentFormModel().lastName,
                        phone: this.studentFormModel().phone,
                    },
                    period: this.studentFormModel().period,
                    course: this.studentFormModel().course,
                    address: this.studentFormModel().address,
                    universityId: this.studentFormModel().universityId!,
                    preferredBoardingStopId: this.student()!.preferredBoardingStop ? this.student()!.preferredBoardingStop!.id : null,
                };

                this.studentService.updateStudent(this.id, updatedStudent).subscribe(() => {
                    this.fetchStudent();
                });
            },
        });
    }

    onDeleteClick() {
        this.confirmDialog.set({
            open: true,
            title: 'Delete student',
            message: `Are you sure you want to delete ${this.student()?.user.firstName} ${this.student()?.user.lastName}? This action cannot be undone.`,
            confirmLabel: 'Delete',
            variant: 'danger',
            action: () => this.deleteUser(this.student()?.user!),
        });
    }

    onToggleUserStatusClick() {
        this.confirmDialog.set({
            open: true,
            title: `${this.student()?.user.active ? 'Deactivate' : 'Activate'} student`,
            message: `Are you sure you want to ${this.student()?.user.active ? 'deactivate' : 'activate'} ${this.student()?.user.firstName} ${this.student()?.user.lastName}? They will ${this.student()?.user.active ? 'lose' : 'gain'} access to the system.`,
            confirmLabel: `${this.student()?.user.active ? 'Deactivate' : 'Activate'}`,
            variant: 'danger',
            action: () => this.toggleUserStatus(this.student()?.user!),
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
            this.fetchStudent();
        });
    }

}
