import { inject, Service } from '@angular/core';

import { environment } from '../../../../environments/environment.development';
import { HttpClient } from '@angular/common/http';
import { Trip, TripCreateRequest, TripUpdateRequest } from '../../../shared/types/trip.type';
import { PageParameters, PageResponse } from '../../../shared/types/page.type';
import { Observable } from 'rxjs/internal/Observable';
import { forkJoin } from 'rxjs/internal/observable/forkJoin';
import { switchMap } from 'rxjs/internal/operators/switchMap';
import { Student } from '../../../shared/types/student.type';
const API_BASE_URL = environment.apiUrl;

@Service()
export class TripService {
    private http = inject(HttpClient);

    getTrips(params: PageParameters): Observable<PageResponse<Trip>> {
        return this.http.get<PageResponse<Trip>>(`${API_BASE_URL}/trips`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getTripById(id: number): Observable<Trip> {
        return this.http.get<Trip>(`${API_BASE_URL}/trips/${id}`);
    }

    getTripByConductorOrVehicle(searchTerm: string, params: PageParameters): Observable<PageResponse<Trip>> {
        return this.http.get<PageResponse<Trip>>(`${API_BASE_URL}/trips/search`, {
            params: {
                searchTerm,
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getStudentsByTripId(id: number, params: PageParameters): Observable<PageResponse<Student>> {
        return this.http.get<PageResponse<Student>>(`${API_BASE_URL}/trips/${id}/students`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getTripsByUser(params: PageParameters): Observable<PageResponse<Trip>> {
        return this.http.get<PageResponse<Trip>>(`${API_BASE_URL}/trips/me`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    updateTripConductor(id: number, trip: TripUpdateRequest): Observable<Trip> {
        return this.http.patch<Trip>(`${API_BASE_URL}/trips/${id}/conductor`, trip);
    }

    updateTripVehicle(id: number, trip: TripUpdateRequest): Observable<Trip> {
        return this.http.patch<Trip>(`${API_BASE_URL}/trips/${id}/vehicle`, trip);
    }

    updateTripAssignment(id: number, conductorId: number, vehicleId: number): Observable<Trip> {
        return this.http.patch<Trip>(`${API_BASE_URL}/trips/${id}/assignment`, { conductorId, vehicleId });
    }

    updateTrip(id: number, trip: TripUpdateRequest): Observable<Trip> {
        const requests: Observable<Trip | null>[] = [];

        if (trip.conductorId !== undefined) {
            requests.push(this.updateTripConductor(id, trip));
        }
        if (trip.vehicleId !== undefined) {
            requests.push(this.updateTripVehicle(id, trip));
        }

        if (requests.length === 0) {
            return this.getTripById(id);
        }

        return forkJoin(requests).pipe(switchMap(() => this.getTripById(id)));
    }


    createTrip(trip: TripCreateRequest): Observable<Trip> {
        return this.http.post<Trip>(`${API_BASE_URL}/trips`, trip);
    }

    deleteTrip(id: number): Observable<void> {
        return this.http.delete<void>(`${API_BASE_URL}/trips/${id}`);
    }
}
