import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

import { PageResponse, PageParameters } from '../../../shared/types/page.type';

import { environment } from '../../../../environments/environment.development';
import { BoardingStop, BoardingStopCreateRequest } from '../../../shared/types/boarding-stop.type';

const API_BASE_URL = environment.apiUrl;

@Service()
export class BoardingStopService {
    private http = inject(HttpClient);

    getBoardingStops(params: PageParameters): Observable<PageResponse<BoardingStop>> {
        return this.http.get<PageResponse<BoardingStop>>(`${API_BASE_URL}/boarding-stops`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getBoardingStopsAsList(): Observable<Array<BoardingStop>> {
        return this.http.get<Array<BoardingStop>>(`${API_BASE_URL}/boarding-stops/list`);
    }

    getBoardingStopsByLocal(local: string, params: PageParameters): Observable<PageResponse<BoardingStop>> {
        return this.http.get<PageResponse<BoardingStop>>(`${API_BASE_URL}/boarding-stops/search`, {
            params: {
                local: local,
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    deleteBoardingStop(id: number): Observable<void> {
        return this.http.delete<void>(`${API_BASE_URL}/boarding-stops/${id}`);
    }

    createBoardingStop(boardingStop: BoardingStopCreateRequest): Observable<BoardingStop> {
        return this.http.post<BoardingStop>(`${API_BASE_URL}/boarding-stops`, boardingStop);
    }
}
