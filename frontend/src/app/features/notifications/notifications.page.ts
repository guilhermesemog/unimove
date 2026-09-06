import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { NotificationService } from '../../core/notifications/notification.service';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { AppNotification } from '../../shared/types/notification.type';

@Component({
  selector: 'app-notifications',
  imports: [DatePipe, HeaderComponent, PaginationComponent, UiStateComponent],
  templateUrl: './notifications.page.html',
})
export class NotificationsPage {
  protected readonly service = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly notifications = signal<AppNotification[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly markingAll = signal(false);
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);
  protected readonly totalPages = signal(0);

  ngOnInit(): void { this.fetch(); }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getMine(this.page(), this.pageSize()).subscribe({
      next: (response) => {
        this.notifications.set(response.content);
        this.totalPages.set(response.page.totalPages);
        this.loading.set(false);
        this.service.refreshUnreadCount();
      },
      error: () => {
        this.error.set('We could not load your notifications.');
        this.loading.set(false);
      },
    });
  }

  protected open(notification: AppNotification): void {
    if (notification.readAt) {
      this.router.navigateByUrl(notification.route);
      return;
    }
    this.markRead(notification, true);
  }

  protected markRead(notification: AppNotification, navigate = false): void {
    this.service.markRead(notification.id).subscribe({
      next: (updated) => {
        this.notifications.update((items) => items.map((item) => item.id === updated.id ? updated : item));
        this.service.unreadCount.update((count) => Math.max(0, count - 1));
        if (navigate) this.router.navigateByUrl(notification.route);
      },
      error: () => this.error.set('We could not mark this notification as read.'),
    });
  }

  protected markAllRead(): void {
    this.markingAll.set(true);
    this.service.markAllRead().subscribe({
      next: () => {
        this.notifications.update((items) => items.map((item) => ({
          ...item,
          readAt: item.readAt ?? new Date().toISOString(),
        })));
        this.service.unreadCount.set(0);
        this.markingAll.set(false);
      },
      error: () => {
        this.error.set('We could not mark all notifications as read.');
        this.markingAll.set(false);
      },
    });
  }

  protected onPageChange(page: number): void { this.page.set(page); this.fetch(); }
  protected onPageSizeChange(size: number): void { this.pageSize.set(size); this.page.set(0); this.fetch(); }
}
