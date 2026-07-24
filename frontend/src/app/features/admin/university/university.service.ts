import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

import { University } from '../../../shared/types/university.type';
import { PageResponse, PageParameters } from '../../../shared/types/page.type';

import { environment } from '../../../../environments/environment.development';
const API_BASE_URL = environment.apiUrl;

@Service()
export class UniversityService {
    private http = inject(HttpClient);

    getUniversities(params: PageParameters): Observable<PageResponse<University>> {
        return this.http.get<PageResponse<University>>(`${API_BASE_URL}/universities`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getUniversitiesAsList(): Observable<Array<University>> {
        return this.http.get<Array<University>>(`${API_BASE_URL}/universities/list`);
    }
}
