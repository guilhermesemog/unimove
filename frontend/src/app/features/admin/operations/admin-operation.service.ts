import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { AdminOperation } from '../../../shared/types/admin-operation.type';

@Injectable({ providedIn: 'root' })
export class AdminOperationService {
  private readonly http = inject(HttpClient);

  getOperations(): Observable<AdminOperation[]> {
    return this.http.get<AdminOperation[]>(`${environment.apiUrl}/admin/operations`);
  }
}
