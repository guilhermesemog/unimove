import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-mobile-header',
  imports: [MatIconModule],
  templateUrl: './mobile-header.component.html',
})
export class MobileHeaderComponent {
  private authService = inject(AuthService);

  logout() {
    this.authService.logout();
  }
}