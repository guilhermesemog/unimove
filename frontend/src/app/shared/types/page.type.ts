export interface PageResponse<T> {
    content: Array<T>;
    page: {
        size: number;
        number: number;
        totalElements: number;
        totalPages: number;
    }
}

export interface PageParameters {
    page: number;
    size: number;
    sortBy: string;
    sortDirection: 'asc' | 'desc';
}
