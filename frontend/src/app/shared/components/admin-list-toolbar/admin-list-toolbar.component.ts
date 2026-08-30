import { Component, input, output } from '@angular/core';

import { Icon } from '../icon/icon';
import { SearchBarComponent } from '../search-bar/search-bar.component';

@Component({
  selector: 'app-admin-list-toolbar',
  standalone: true,
  imports: [Icon, SearchBarComponent],
  templateUrl: './admin-list-toolbar.component.html',
})
export class AdminListToolbarComponent {
  readonly value = input.required<string>();
  readonly placeholder = input('Search...');
  readonly createLabel = input.required<string>();

  readonly valueChange = output<string>();
  readonly search = output<void>();
  readonly clear = output<void>();
  readonly create = output<void>();
}
