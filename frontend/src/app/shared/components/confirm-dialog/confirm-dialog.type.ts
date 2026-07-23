export type ConfirmDialogVariant = 'default' | 'danger';

export interface ConfirmDialogState {
    open: boolean;
    title: string;
    message: string;
    confirmLabel: string;
    variant: ConfirmDialogVariant;
    action: (() => void) | null;
}

export const CLOSED_DIALOG: ConfirmDialogState = {
    open: false,
    title: '',
    message: '',
    confirmLabel: 'Confirm',
    variant: 'default',
    action: null,
};