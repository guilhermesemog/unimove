import { Component, input, model } from '@angular/core';
import { FormValueControl, WithOptionalFieldTree, ValidationError } from '@angular/forms/signals';
import { CommonModule } from '@angular/common';
import { FieldErrorsComponent } from '../field-errors/field-errors.component';

@Component({
  selector: 'app-number-field',
  standalone: true,
  imports: [CommonModule, FieldErrorsComponent],
  templateUrl: './number-field.component.html',
})
export class NumberFieldComponent implements FormValueControl<number> {
  readonly value = model<number>(0);

  readonly errors = input<readonly WithOptionalFieldTree<ValidationError>[]>([]);
  readonly touched = model<boolean>(false);
  readonly disabled = input<boolean>(false);
  readonly required = input<boolean>(false);

  readonly label = input.required<string>();
  readonly placeholder = input<string>('');
  readonly type = input<string>('number');
  readonly id = input<string>();
  readonly min = input<number>();
}
