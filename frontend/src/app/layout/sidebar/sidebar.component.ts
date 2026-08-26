import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { BrandComponent } from '../../shared/components/brand/brand.component';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, BrandComponent, Icon],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private authService = inject(AuthService);
  protected readonly copy = UI_COPY;

  @Input() isOpen = false;
  @Output() closeDrawer = new EventEmitter<void>();

  logout() {
    this.authService.logout();
  }

  onLinkClick() {
    this.closeDrawer.emit();
  }

  get asideClasses(): string {
    const base =
      'fixed inset-y-0 left-0 z-40 w-72 h-screen bg-white border-r border-slate-200 flex flex-col shadow-2xl shadow-navy-950/10 ' +
      'transform transition-transform duration-200 ease-in-out ' +
      'lg:sticky lg:top-0 lg:translate-x-0 lg:z-auto lg:shadow-none';
    return `${base} ${this.isOpen ? 'translate-x-0' : '-translate-x-full'}`;
  }
}
