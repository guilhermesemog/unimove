import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { BrandComponent } from '../../shared/components/brand/brand.component';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-mobile-header',
  imports: [BrandComponent, Icon, NotificationBellComponent],
  templateUrl: './mobile-header.component.html',
})
export class MobileHeaderComponent {
  private authService = inject(AuthService);
  protected readonly copy = UI_COPY;

  logout() {
    this.authService.logout();
  }
}
