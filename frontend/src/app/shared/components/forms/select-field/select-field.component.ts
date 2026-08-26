import { Component, input, model } from '@angular/core';
import { FormValueControl, WithOptionalFieldTree, ValidationError } from '@angular/forms/signals';
import { CommonModule } from '@angular/common';
import { FieldErrorsComponent } from '../field-errors/field-errors.component';

export interface SelectOption<T> {
  value: T;
  label: string;
}

@Component({
  selector: 'app-select-field',
  standalone: true,
  imports: [CommonModule, FieldErrorsComponent],
  templateUrl: './select-field.component.html',
})
export class SelectFieldComponent<T> implements FormValueControl<T | null> {
  readonly value = model<T | null>(null);

  readonly errors = input<readonly WithOptionalFieldTree<ValidationError>[]>([]);
  readonly touched = model<boolean>(false);
  readonly disabled = input<boolean>(false);
  readonly required = input<boolean>(false);

  readonly label = input.required<string>();
  readonly placeholder = input<string>('Select an option');
  readonly id = input<string>();
  readonly options = input.required<ReadonlyArray<SelectOption<T>>>();

  onChange(event: Event) {
    const index = Number((event.target as HTMLSelectElement).value);
    this.value.set(index === -1 ? null : this.options()[index].value);
    this.touched.set(true);
  }

  indexOfValue(): number {
    if (this.value() === null) {
      return -1;
    }
    return this.options().findIndex((option) => option.value === this.value());
  }
}
