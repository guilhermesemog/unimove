import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

import { environment } from '../../../../environments/environment.development';
import { Booking, BookingCreateRequest } from '../../../shared/types/booking.type';
import { PageParameters, PageResponse } from '../../../shared/types/page.type';

const API_BASE_URL = environment.apiUrl;

@Service()
export class BookingService {
    private http = inject(HttpClient);

    createBooking(booking: BookingCreateRequest): Observable<Booking> {
        return this.http.post<Booking>(`${API_BASE_URL}/bookings`, booking);
    }


    getBookingsByInterestListId(interestListId: number, params: PageParameters): Observable<PageResponse<Booking>> {
        return this.http.get<PageResponse<Booking>>(`${API_BASE_URL}/bookings/interest-list/${interestListId}`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getUserBookings(params: PageParameters): Observable<PageResponse<Booking>> {
        return this.http.get<PageResponse<Booking>>(`${API_BASE_URL}/bookings/me`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }
}