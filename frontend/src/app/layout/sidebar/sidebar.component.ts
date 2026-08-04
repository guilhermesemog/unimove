import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';


@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
})
export class Sidebar {
  authService = inject(AuthService);
  isAdmin = this.authService.identify().then((user) => user.role === 'ADMIN');

  logout() {
    this.authService.logout();
  }
}
