import { Component, computed, inject, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { UserRole } from '../../shared/types/user.type';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';
import { NotificationService } from '../../core/notifications/notification.service';

@Component({
  selector: 'app-mobile-nav',
  imports: [RouterLink, RouterLinkActive, Icon],
  templateUrl: './mobile-nav.component.html',
})
export class MobileNavComponent {
  protected readonly notifications = inject(NotificationService);
  userRole = input<UserRole | null>(null);
  protected readonly copy = UI_COPY;
  protected readonly isStudent = computed(() => this.userRole() === UserRole.Student);
  protected readonly isConductor = computed(() => this.userRole() === UserRole.Conductor);
}
