import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { PageParameters, PageResponse } from '../../../shared/types/page.type';
import {
  RecurrenceGeneration,
  RecurrencePlan,
  RecurrencePlanRequest,
  RecurrencePlanStatus,
  RecurrencePreview,
} from '../../../shared/types/recurrence-plan.type';

@Injectable({ providedIn: 'root' })
export class RecurrencePlanService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/admin/recurrence-plans`;

  getAll(params: PageParameters): Observable<PageResponse<RecurrencePlan>> {
    return this.http.get<PageResponse<RecurrencePlan>>(this.endpoint, {
      params: { page: params.page, size: params.size, sortDirection: params.sortDirection },
    });
  }

  getById(id: string): Observable<RecurrencePlan> {
    return this.http.get<RecurrencePlan>(`${this.endpoint}/${id}`);
  }

  preview(request: RecurrencePlanRequest): Observable<RecurrencePreview> {
    return this.http.post<RecurrencePreview>(`${this.endpoint}/preview`, request);
  }

  create(request: RecurrencePlanRequest): Observable<RecurrencePlan> {
    return this.http.post<RecurrencePlan>(this.endpoint, request);
  }

  update(id: string, request: RecurrencePlanRequest): Observable<RecurrencePlan> {
    return this.http.put<RecurrencePlan>(`${this.endpoint}/${id}`, request);
  }

  updateStatus(id: string, status: RecurrencePlanStatus): Observable<RecurrencePlan> {
    return this.http.patch<RecurrencePlan>(`${this.endpoint}/${id}/status`, { status });
  }

  generate(id: string): Observable<RecurrenceGeneration> {
    return this.http.post<RecurrenceGeneration>(`${this.endpoint}/${id}/generate`, {});
  }

  archive(id: string): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }
}
