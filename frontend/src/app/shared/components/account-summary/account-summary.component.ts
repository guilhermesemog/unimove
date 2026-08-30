import { Component, input, output } from '@angular/core';

import { StatusChipComponent } from '../status-chip/status-chip.component';

@Component({
  selector: 'app-account-summary',
  standalone: true,
  imports: [StatusChipComponent],
  templateUrl: './account-summary.component.html',
})
export class AccountSummaryComponent {
  readonly role = input.required<string>();
  readonly active = input.required<boolean>();
  readonly statusChange = output<void>();

  protected roleLabel() {
    return this.role().toUpperCase() === 'CONDUCTOR' ? 'Driver' : this.role().toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }
}
