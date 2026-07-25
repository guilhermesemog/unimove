import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

import { environment } from '../../../../environments/environment.development';
import { Conductor, ConductorCreateRequest, ConductorUpdateRequest } from '../../../shared/types/conductor.type';

const API_BASE_URL = environment.apiUrl;

@Service()
export class ConductorService {
    private http = inject(HttpClient);

    getConductorById(userId: number): Observable<Conductor> {
        return this.http.get<Conductor>(`${API_BASE_URL}/conductors/${userId}`);
    }

    updateConductor(userId: number, conductor: Conductor): Observable<void> {
        const updatableConductor: ConductorUpdateRequest = {
            user: {
                cpf: conductor.user.cpf,
                firstName: conductor.user.firstName,
                lastName: conductor.user.lastName,
                phone: conductor.user.phone
            },
            license: conductor.license,
            licenseExpirationDate: conductor.licenseExpirationDate,
        };

        return this.http.put<void>(`${API_BASE_URL}/conductors/${userId}`, updatableConductor);
    }

    createConductor(conductor: ConductorCreateRequest): Observable<Conductor> {
        return this.http.post<Conductor>(`${API_BASE_URL}/conductors`, conductor);
    }
}