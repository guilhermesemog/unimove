import { Component, input, model } from '@angular/core';
import { FormValueControl, WithOptionalFieldTree, ValidationError } from '@angular/forms/signals';
import { CommonModule } from '@angular/common';
import { FieldErrorsComponent } from '../field-errors/field-errors.component';

@Component({
    selector: 'app-date-field',
    standalone: true,
    imports: [CommonModule, FieldErrorsComponent],
    templateUrl: './date-field.component.html',
})
export class DateFieldComponent implements FormValueControl<string> {
    readonly value = model<string>('');

    readonly errors = input<readonly WithOptionalFieldTree<ValidationError>[]>([]);
    readonly touched = model<boolean>(false);
    readonly disabled = input<boolean>(false);
    readonly required = input<boolean>(false);

    readonly label = input.required<string>();
    readonly id = input<string>();
    readonly min = input<string>();
    readonly max = input<string>();

    onInput(event: Event) {
        this.value.set((event.target as HTMLInputElement).value);
    }
}
