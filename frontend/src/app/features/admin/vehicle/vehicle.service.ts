import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';

import { Observable } from 'rxjs';
import { PageParameters, PageResponse } from '../../../shared/types/page.type';
import { Vehicle, VehicleCreateRequest, VehicleUpdateRequest } from '../../../shared/types/vehicle.type';

import { environment } from '../../../../environments/environment.development';
const API_BASE_URL = environment.apiUrl;

@Service()
export class VehicleService {
    private http = inject(HttpClient);

    getVehicles(params: PageParameters): Observable<PageResponse<Vehicle>> {
        return this.http.get<PageResponse<Vehicle>>(`${API_BASE_URL}/vehicles`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getVehiclesByPlate(plate: string, params: PageParameters): Observable<PageResponse<Vehicle>> {
        return this.http.get<PageResponse<Vehicle>>(`${API_BASE_URL}/vehicles/search`, {
            params: {
                plate: plate,
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getVehiclesAsList(): Observable<Array<Vehicle>> {
        return this.http.get<Array<Vehicle>>(`${API_BASE_URL}/vehicles/list`);
    }

    getVehicleById(id: string): Observable<Vehicle> {
        return this.http.get<Vehicle>(`${API_BASE_URL}/vehicles/${id}`);
    }

    updateVehicle(id: string, vehicle: VehicleUpdateRequest): Observable<Vehicle> {
        return this.http.put<Vehicle>(`${API_BASE_URL}/vehicles/${id}`, vehicle);
    }

    deleteVehicle(id: string): Observable<void> {
        return this.http.delete<void>(`${API_BASE_URL}/vehicles/${id}`);
    }

    createVehicle(vehicle: VehicleCreateRequest): Observable<Vehicle> {
        return this.http.post<Vehicle>(`${API_BASE_URL}/vehicles`, vehicle);
    }
}
