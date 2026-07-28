import { signal } from '@angular/core';
import { ConfirmDialogState, CLOSED_DIALOG } from '../components/confirm-dialog/confirm-dialog.type';

export class ConfirmDialogController {
  readonly state = signal<ConfirmDialogState>(CLOSED_DIALOG);

  open(config: Omit<ConfirmDialogState, 'open'>) {
    this.state.set({ ...config, open: true });
  }

  confirm() {
    this.state().action?.();
    this.close();
  }

  cancel() {
    this.close();
  }

  private close() {
    this.state.set(CLOSED_DIALOG);
  }
}