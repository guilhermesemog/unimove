import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private authService = inject(AuthService);

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
      'fixed inset-y-0 left-0 z-40 w-64 h-screen bg-white border-r border-gray-200 flex flex-col ' +
      'transform transition-transform duration-200 ease-in-out ' +
      'lg:sticky lg:top-0 lg:translate-x-0 lg:z-auto';
    return `${base} ${this.isOpen ? 'translate-x-0' : '-translate-x-full'}`;
  }
}