import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { MobileHeaderComponent } from '../mobile-header/mobile-header.component';
import { MobileNavComponent } from '../mobile-nav/mobile-nav.component';
import { AuthService } from '../../core/auth/auth.service';
import { UserRole } from '../../shared/types/user.type';
import { MobileAdminHeaderComponent } from '../mobile-admin-header/mobile-admin-header.component';
import { BrandComponent } from '../../shared/components/brand/brand.component';
import { UI_COPY } from '../../core/content/ui-copy';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, SidebarComponent, MobileHeaderComponent, MobileNavComponent, MobileAdminHeaderComponent, BrandComponent],
  templateUrl: './main-layout.component.html',
})
export class MainLayout {
  protected readonly copy = UI_COPY;
  private authService = inject(AuthService);
  isAdmin = signal<boolean>(false);
  isReady = signal<boolean>(false);
  isDrawerOpen = signal<boolean>(false);
  userRole = signal<UserRole | null>(null);

  ngOnInit() {
    this.authService.identify().subscribe((user) => {
      this.userRole.set(user.role);
      this.isAdmin.set(this.userRole() === UserRole.Admin);
      this.isReady.set(true);
    });
  }

  toggleDrawer() {
    this.isDrawerOpen.update((v) => !v);
    document.body.classList.toggle('overflow-hidden', this.isDrawerOpen());
  }

  closeDrawer() {
    this.isDrawerOpen.set(false);
    document.body.classList.remove('overflow-hidden');
  }
}
