import { Component, computed, input, model, signal } from '@angular/core';
import { FormValueControl, WithOptionalFieldTree, ValidationError } from '@angular/forms/signals';
import { CommonModule } from '@angular/common';
import { Icon } from '../../icon/icon';
import { FieldErrorsComponent } from '../field-errors/field-errors.component';

@Component({
  selector: 'app-text-field',
  standalone: true,
  imports: [CommonModule, Icon, FieldErrorsComponent],
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

  protected readonly showPassword = signal(false);

  protected readonly hasError = computed(() => this.touched() && this.errors().length > 0);

  protected readonly resolvedType = computed(() =>
    this.type() === 'password' && this.showPassword() ? 'text' : this.type()
  );

  protected toggleShowPassword(): void {
    this.showPassword.update((v) => !v);
  }
}
