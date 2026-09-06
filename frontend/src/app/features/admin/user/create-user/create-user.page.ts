import { Component, computed, inject, signal } from '@angular/core';
import { form, FormField, maxLength, minLength, required } from '@angular/forms/signals';
import { TextFieldComponent } from '../../../../shared/components/forms/text-field/text-field.component';
import { SelectFieldComponent, SelectOption } from '../../../../shared/components/forms/select-field/select-field.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { CpfPipe } from '../../../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../../../shared/pipes/phone-pipe';
import { ConductorCreateRequest } from '../../../../shared/types/conductor.type';
import { StudentCreateRequest } from '../../../../shared/types/student.type';
import { UserCommomCreate, UserCreateRequest, UserRole } from '../../../../shared/types/user.type';
import { UniversityService } from '../../university/university.service';
import { University } from '../../../../shared/types/university.type';
import { DateFieldComponent } from '../../../../shared/components/forms/date-field/date-field.component';
import { NumberFieldComponent } from '../../../../shared/components/forms/number-field/number-field.component';
import { BoardingStopService } from '../../boarding-stop/boarding-stop.service';
import { BoardingStop } from '../../../../shared/types/boarding-stop.type';
import { UserService } from '../user.service';
import { StudentService } from '../../student/student.service';
import { ConductorService } from '../../conductor/conductor.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-create-user',
    imports: [FormField, HeaderComponent, TextFieldComponent, NumberFieldComponent, DateFieldComponent, SelectFieldComponent, CpfPipe, PhonePipe],
    templateUrl: './create-user.page.html',
})
export class CreateUserPage {
    router = inject(Router);

    commomUserFields = signal<UserCommomCreate | null>(null);
    conductor = signal<ConductorCreateRequest | null>(null);
    student = signal<StudentCreateRequest | null>(null);

    userService = inject(UserService);
    studentService = inject(StudentService);
    conductorService = inject(ConductorService);

    universityService = inject(UniversityService);
    universities = signal<Array<University>>([]);
    universityOptions = computed<SelectOption<string>[]>(() =>
        this.universities().map((u) => ({ value: u.id, label: u.name }))
    );

    boardingStopService = inject(BoardingStopService);
    boardingStops = signal<Array<BoardingStop>>([]);
    boardingStopOptions = computed<SelectOption<string>[]>(() =>
        this.boardingStops().map((bs) => ({ value: bs.id, label: bs.local }))
    );

    roleOptions = [
        { value: UserRole.Admin, label: 'Admin' },
        { value: UserRole.Student, label: 'Student' },
        { value: UserRole.Conductor, label: 'Driver' },
    ];

    roleFormModel = signal<UserRole | null>(null);

    roleForm = form(this.roleFormModel, (schema) => {
        required(schema, { message: 'Role is required' });
    });

    // User Form

    userFormModel = signal({
        password: this.commomUserFields()?.password || '',
        cpf: this.commomUserFields()?.cpf || '',
        firstName: this.commomUserFields()?.firstName || '',
        lastName: this.commomUserFields()?.lastName || '',
        phone: this.commomUserFields()?.phone || '',
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
        required(schema.password, { message: 'Password is required' });
        minLength(schema.password, 8, { message: 'Password must be at least 8 characters long' });
        maxLength(schema.password, 20, { message: 'Password must be at most 20 characters long' });
    });

    // Conductor Form

    conductorFormModel = signal({
        license: this.conductor()?.license || '',
        licenseExpirationDate: this.conductor()?.licenseExpirationDate || '',
    });

    conductorForm = form(this.conductorFormModel, (schema) => {
        required(schema.license, { message: 'License is required' });
        required(schema.licenseExpirationDate, { message: 'License expiration date is required' });
    });

    // Student Form

    studentFormModel = signal({
        period: this.student()?.period || 0,
        course: this.student()?.course || '',
        address: this.student()?.address || '',
        universityId: this.student()?.universityId ?? null,
        preferredBoardingStopId: this.student()?.preferredBoardingStopId ?? null,
    });

    studentForm = form(this.studentFormModel, (schema) => {
        required(schema.period, { message: 'Period is required' });
        required(schema.course, { message: 'Course is required' });
        required(schema.address, { message: 'Address is required' });
        required(schema.universityId, { message: 'University is required' });
    });

    fetchUniversities() {
        this.universityService.getUniversitiesAsList().subscribe((response) => {
            this.universities.set(response);
        });
    }

    fetchBoardingStops() {
        this.boardingStopService.getBoardingStopsAsList().subscribe((response) => {
            this.boardingStops.set(response);
        });
    }

    ngOnInit() {
        this.fetchUniversities();
        this.fetchBoardingStops();
    }

    onSubmit() {
        const formType = this.roleForm().value();

        if (formType === UserRole.Admin) {
            if (!this.userForm().valid()) {
                return;
            }

            const newUser: UserCreateRequest = {
                user: {
                    cpf: this.userFormModel().cpf,
                    firstName: this.userFormModel().firstName,
                    lastName: this.userFormModel().lastName,
                    phone: this.userFormModel().phone,
                    password: this.userFormModel().password,
                },
                role: UserRole.Admin,
            };

            this.userService.createUser(newUser).subscribe((response) => {
                this.router.navigate(['/admin/users']);
            });

            return;
        }

        if (formType === UserRole.Conductor) {
            if (!this.userForm().valid() || !this.conductorForm().valid()) {
                return;
            }

            const newConductor: ConductorCreateRequest = {
                user: {
                    cpf: this.userFormModel().cpf,
                    firstName: this.userFormModel().firstName,
                    lastName: this.userFormModel().lastName,
                    phone: this.userFormModel().phone,
                    password: this.userFormModel().password,
                },
                license: this.conductorFormModel().license,
                licenseExpirationDate: this.conductorFormModel().licenseExpirationDate,
            };

            this.conductorService.createConductor(newConductor).subscribe((response) => {
                this.router.navigate(['/admin/users']);
            });

            return;
        }

        if (formType === UserRole.Student) {
            if (!this.userForm().valid() || !this.studentForm().valid()) {
                return;
            }

            const newStudent: StudentCreateRequest = {
                user: {
                    cpf: this.userFormModel().cpf,
                    firstName: this.userFormModel().firstName,
                    lastName: this.userFormModel().lastName,
                    phone: this.userFormModel().phone,
                    password: this.userFormModel().password,
                },
                period: this.studentFormModel().period,
                course: this.studentFormModel().course,
                address: this.studentFormModel().address,
                universityId: this.studentFormModel().universityId!,
                preferredBoardingStopId: this.studentFormModel().preferredBoardingStopId ?? null,
            };

            this.studentService.createStudent(newStudent).subscribe((response) => {
                this.router.navigate(['/admin/users']);
            });

            return;
        }
    }

    isFormInvalid = computed(() => {
        const role = this.roleForm().value();

        let invalid = false;

        if (role === UserRole.Conductor) {
            invalid = this.conductorForm().invalid() || !this.conductorForm().dirty();
        }
        if (role === UserRole.Student) {
            invalid = this.studentForm().invalid() || !this.studentForm().dirty();
        }

        return this.roleForm().invalid() || this.userForm().invalid() || !this.userForm().dirty() || invalid;

    });
}
