import { Service, signal } from '@angular/core';

export interface ErrorDialogState {
    open: boolean;
    title: string;
    message: string;
}

interface ShowOptions {
    onConfirm?: () => void;
}

const CLOSED_ERROR_DIALOG: ErrorDialogState = {
    open: false,
    title: '',
    message: '',
};

@Service()
export class ErrorDialogService {
    state = signal<ErrorDialogState>(CLOSED_ERROR_DIALOG);
    private onConfirmAction?: () => void;

    show(title: string, message: string, options?: ShowOptions) {
        this.onConfirmAction = options?.onConfirm;
        this.state.set({ open: true, title, message });
    }

    confirm() {
        this.onConfirmAction?.();
        this.close();
    }

    close() {
        this.onConfirmAction = undefined;
        this.state.set(CLOSED_ERROR_DIALOG);
    }
}