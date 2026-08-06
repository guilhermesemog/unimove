import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { UserRole } from '../../shared/types/user.type';


@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
})
export class Sidebar {
  private authService = inject(AuthService);
  isAdmin = signal<boolean>(false);

  ngOnInit() {
    this.authService.identify().then((user) => {
      this.isAdmin.set(user.role === UserRole.Admin);
    });
  }

  logout() {
    this.authService.logout();
  }
}
