import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { BrandComponent } from '../../shared/components/brand/brand.component';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';

@Component({
  selector: 'app-mobile-header',
  imports: [BrandComponent, Icon],
  templateUrl: './mobile-header.component.html',
})
export class MobileHeaderComponent {
  private authService = inject(AuthService);
  protected readonly copy = UI_COPY;

  logout() {
    this.authService.logout();
  }
}
