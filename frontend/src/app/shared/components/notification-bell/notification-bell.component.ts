import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification.service';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-notification-bell',
  imports: [RouterLink, Icon],
  templateUrl: './notification-bell.component.html',
})
export class NotificationBellComponent {
  protected readonly notifications = inject(NotificationService);
}
