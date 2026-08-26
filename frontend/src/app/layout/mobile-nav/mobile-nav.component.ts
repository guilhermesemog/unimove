import { Component, input, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { UserRole } from '../../shared/types/user.type';

@Component({
  selector: 'app-mobile-nav',
  imports: [RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './mobile-nav.component.html',
})
export class MobileNavComponent {
  userRole = input<UserRole | null>(null);
  isStudent = signal<boolean>(false);
  isConductor = signal<boolean>(false);

  ngOnInit() {
    this.isStudent.set(this.userRole() === UserRole.Student);
    this.isConductor.set(this.userRole() === UserRole.Conductor);
  }
}