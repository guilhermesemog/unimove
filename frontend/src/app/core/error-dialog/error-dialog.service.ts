import { Service, signal } from '@angular/core';

export interface ErrorDialogState {
    open: boolean;
    title: string;
    message: string;
}

const CLOSED_ERROR_DIALOG: ErrorDialogState = {
    open: false,
    title: '',
    message: '',
};

@Service()
export class ErrorDialogService {
    state = signal<ErrorDialogState>(CLOSED_ERROR_DIALOG);

    show(title: string, message: string) {
        this.state.set({ open: true, title, message });
    }

    close() {
        this.state.set(CLOSED_ERROR_DIALOG);
    }
}