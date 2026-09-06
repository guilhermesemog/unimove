import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { OperationalAnalytics } from '../../../shared/types/analytics.type';

export type AnalyticsRequest = { days: 7 | 30 | 90 } | { from: string; to: string };

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/admin/analytics`;

  get(request: AnalyticsRequest): Observable<OperationalAnalytics> {
    let params = new HttpParams();
    if ('days' in request) params = params.set('days', request.days);
    else params = params.set('from', request.from).set('to', request.to);
    return this.http.get<OperationalAnalytics>(this.endpoint, { params });
  }
}
