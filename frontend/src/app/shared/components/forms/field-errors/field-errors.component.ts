import { Component, input } from '@angular/core';
import { ValidationError, WithOptionalFieldTree } from '@angular/forms/signals';

import { Icon } from '../../icon/icon';

@Component({
  selector: 'app-field-errors',
  standalone: true,
  imports: [Icon],
  templateUrl: './field-errors.component.html',
})
export class FieldErrorsComponent {
  readonly errors = input.required<readonly WithOptionalFieldTree<ValidationError>[]>();
}
