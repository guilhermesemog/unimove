import { Component, EventEmitter, inject, Output } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { BrandComponent } from '../../shared/components/brand/brand.component';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-mobile-admin-header',
  imports: [BrandComponent, Icon, NotificationBellComponent],
  templateUrl: './mobile-admin-header.component.html',
})
export class MobileAdminHeaderComponent {
  private authService = inject(AuthService);
  protected readonly copy = UI_COPY;
  @Output() menuToggle = new EventEmitter<void>();

  logout() {
    this.authService.logout();
  }
}
