import { Component, HostListener, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ConfirmDialogVariant = 'default' | 'danger';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
})
export class ConfirmDialogComponent {
  open = input.required<boolean>();
  title = input('Are you sure?');
  message = input('This action cannot be undone.');
  confirmLabel = input('Confirm');
  cancelLabel = input('Cancel');
  variant = input<ConfirmDialogVariant>('default');

  confirm = output<void>();
  cancel = output<void>();

  @HostListener('document:keydown.escape')
  onEscape() {
    if (this.open()) {
      this.onCancel();
    }
  }

  onConfirm() {
    this.confirm.emit();
  }

  onCancel() {
    this.cancel.emit();
  }
}