export interface TableColumn<T> {
    key: Extract<keyof T, string>;
    label: string;
    sortable?: boolean;
    align?: 'left' | 'center' | 'right';
    width?: string;
    format?: (row: T) => string;
}