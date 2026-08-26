import { Component, EventEmitter, inject, Output } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { BrandComponent } from '../../shared/components/brand/brand.component';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';

@Component({
  selector: 'app-mobile-admin-header',
  imports: [BrandComponent, Icon],
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
