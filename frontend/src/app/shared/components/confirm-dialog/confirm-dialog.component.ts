import { Component, HostListener, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { A11yModule } from '@angular/cdk/a11y';

import { ConfirmDialogVariant } from './confirm-dialog.type';
import { UI_COPY } from '../../../core/content/ui-copy';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, A11yModule, Icon],
  templateUrl: './confirm-dialog.component.html',
})
export class ConfirmDialogComponent {
  protected readonly copy = UI_COPY;
  open = input.required<boolean>();
  title = input('Are you sure?');
  message = input('This action cannot be undone.');
  confirmLabel = input('Confirm');
  cancelLabel = input('Cancel');
  variant = input<ConfirmDialogVariant>('default');
  hideCancel = input(false);

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
