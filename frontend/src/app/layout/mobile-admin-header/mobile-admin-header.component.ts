import { Component, EventEmitter, inject, Output } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-mobile-admin-header',
  imports: [MatIconModule],
  templateUrl: './mobile-admin-header.component.html',
})
export class MobileAdminHeaderComponent {
  private authService = inject(AuthService);
  @Output() menuToggle = new EventEmitter<void>();

  logout() {
    this.authService.logout();
  }
}
