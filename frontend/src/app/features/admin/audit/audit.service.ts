import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { AuditEvent, AuditEventDetail, AuditFilters } from '../../../shared/types/audit.type';
import { PageResponse } from '../../../shared/types/page.type';

@Injectable({ providedIn: 'root' })
export class AuditEventService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/admin/audit-events`;

  getAll(filters: AuditFilters, page: number, size: number, sortDirection: 'asc' | 'desc' = 'desc'):
    Observable<PageResponse<AuditEvent>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sortDirection', sortDirection);
    const scalarFilters: Array<[string, string | undefined]> = [
      ['from', filters.from], ['to', filters.to], ['actor', filters.actor], ['action', filters.action],
      ['entityType', filters.entityType], ['entityId', filters.entityId], ['correlationId', filters.correlationId],
    ];
    scalarFilters.forEach(([key, value]) => { if (value) params = params.set(key, value); });
    filters.entityIds?.forEach((id) => { params = params.append('entityIds', id); });
    return this.http.get<PageResponse<AuditEvent>>(this.endpoint, { params });
  }

  getById(id: string): Observable<AuditEventDetail> {
    return this.http.get<AuditEventDetail>(`${this.endpoint}/${id}`);
  }
}
