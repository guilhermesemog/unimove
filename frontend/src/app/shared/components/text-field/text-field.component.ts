import { Component, input, model } from '@angular/core';
import { FormValueControl, WithOptionalFieldTree, ValidationError } from '@angular/forms/signals';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-text-field',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './text-field.component.html',
})
export class TextFieldComponent implements FormValueControl<string> {
  readonly value = model<string>('');

  readonly errors = input<readonly WithOptionalFieldTree<ValidationError>[]>([]);
  readonly touched = model<boolean>(false);
  readonly disabled = input<boolean>(false);
  readonly required = input<boolean>(false);

  readonly label = input.required<string>();
  readonly placeholder = input<string>('');
  readonly type = input<string>('text');
  readonly id = input<string>();
}