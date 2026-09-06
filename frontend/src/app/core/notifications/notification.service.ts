import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { environment } from '../../../environments/environment.development';
import { AppNotification } from '../../shared/types/notification.type';
import { PageResponse } from '../../shared/types/page.type';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/notifications`;
  private polling?: Subscription;
  private readonly focusHandler = () => this.refreshUnreadCount();

  readonly unreadCount = signal(0);

  getMine(page: number, size: number): Observable<PageResponse<AppNotification>> {
    return this.http.get<PageResponse<AppNotification>>(`${this.endpoint}/me`, { params: { page, size } });
  }

  markRead(id: string): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${this.endpoint}/${id}/read`, {});
  }

  markAllRead(): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/me/read-all`, {});
  }

  refreshUnreadCount(): void {
    this.http.get<{ count: number }>(`${this.endpoint}/me/unread-count`).subscribe({
      next: ({ count }) => this.unreadCount.set(count),
    });
  }

  startUpdates(): void {
    if (this.polling) return;
    this.polling = timer(0, 60_000).pipe(
      switchMap(() => this.http.get<{ count: number }>(`${this.endpoint}/me/unread-count`)),
    ).subscribe({ next: ({ count }) => this.unreadCount.set(count) });
    window.addEventListener('focus', this.focusHandler);
  }

  stopUpdates(): void {
    this.polling?.unsubscribe();
    this.polling = undefined;
    window.removeEventListener('focus', this.focusHandler);
  }
}
