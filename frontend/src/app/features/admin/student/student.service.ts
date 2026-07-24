import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

import { Student, StudentUpdateRequest } from '../../../shared/types/student.type';
import { PageResponse, PageParameters } from '../../../shared/types/page.type';

import { environment } from '../../../../environments/environment.development';
const API_BASE_URL = environment.apiUrl;

@Service()
export class StudentService {
    private http = inject(HttpClient);

    getStudentById(userId: number): Observable<Student> {
        return this.http.get<Student>(`${API_BASE_URL}/students/${userId}`);
    }

    updateStudent(userId: number, student: Student): Observable<void> {
        const updatableStudent: StudentUpdateRequest = {
            user: {
                cpf: student.user.cpf,
                firstName: student.user.firstName,
                lastName: student.user.lastName,
                phone: student.user.phone
            },
            period: student.period,
            course: student.course,
            address: student.address,
            universityId: student.universityId
        };

        return this.http.put<void>(`${API_BASE_URL}/students/${userId}`, updatableStudent);
    }

}
