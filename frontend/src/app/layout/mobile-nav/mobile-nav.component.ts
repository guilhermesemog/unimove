import { Component, computed, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { UserRole } from '../../shared/types/user.type';
import { UI_COPY } from '../../core/content/ui-copy';
import { Icon } from '../../shared/components/icon/icon';

@Component({
  selector: 'app-mobile-nav',
  imports: [RouterLink, RouterLinkActive, Icon],
  templateUrl: './mobile-nav.component.html',
})
export class MobileNavComponent {
  userRole = input<UserRole | null>(null);
  protected readonly copy = UI_COPY;
  protected readonly isStudent = computed(() => this.userRole() === UserRole.Student);
  protected readonly isConductor = computed(() => this.userRole() === UserRole.Conductor);
}
