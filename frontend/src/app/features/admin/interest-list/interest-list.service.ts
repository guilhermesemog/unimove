import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';


import { environment } from '../../../../environments/environment.development';
import { PageParameters, PageResponse } from '../../../shared/types/page.type';
import { Observable } from 'rxjs';
import { InterestList, InterestListCreateRequest, InterestListUpdateRequest } from '../../../shared/types/interest-list.type';
const API_BASE_URL = environment.apiUrl;


@Service()
export class InterestListService {
    private http = inject(HttpClient);

    getInterestLists(params: PageParameters): Observable<PageResponse<InterestList>> {
        return this.http.get<PageResponse<InterestList>>(`${API_BASE_URL}/interest-lists`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getInterestListById(interestListId: number): Observable<InterestList> {
        return this.http.get<InterestList>(`${API_BASE_URL}/interest-lists/${interestListId}`);
    }

    createInterestList(interestList: InterestListCreateRequest): Observable<InterestList> {
        return this.http.post<InterestList>(`${API_BASE_URL}/interest-lists`, interestList);
    }

    updateInterestList(interestListId: number, interestList: InterestListUpdateRequest): Observable<void> {
        return this.http.put<void>(`${API_BASE_URL}/interest-lists/${interestListId}`, interestList);
    }

    deleteInterestList(interestListId: number): Observable<void> {
        return this.http.delete<void>(`${API_BASE_URL}/interest-lists/${interestListId}`);
    }

    toggleInterestListStatus(interestListId: number): Observable<void> {
        return this.http.patch<void>(`${API_BASE_URL}/interest-lists/${interestListId}/toggle-status`, {});
    }
}
