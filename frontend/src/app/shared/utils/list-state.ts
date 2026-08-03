import { signal } from '@angular/core';
import { Observable } from 'rxjs';

import { PageResponse, PageParameters } from '../types/page.type';

export interface ListStateConfig<T> {
    initialSortBy: string;
    initialPageSize?: number;
    initialSortDirection?: 'asc' | 'desc';
    fetchAll: (query: PageParameters) => Observable<PageResponse<T>>;
    fetchByQuery: (term: string, query: PageParameters) => Observable<PageResponse<T>>;
}

export class ListState<T> {
    readonly items = signal<T[]>([]);
    readonly totalPages = signal(0);
    readonly page = signal(0);
    readonly pageSize;
    readonly sortBy;
    readonly sortDirection = signal<'asc' | 'desc'>('asc');
    readonly searchTerm = signal('');

    private readonly fetchAll: ListStateConfig<T>['fetchAll'];
    private readonly fetchByQuery: ListStateConfig<T>['fetchByQuery'];

    constructor(config: ListStateConfig<T>) {
        this.fetchAll = config.fetchAll;
        this.fetchByQuery = config.fetchByQuery;
        this.pageSize = signal(config.initialPageSize ?? 10);
        this.sortDirection = signal(config.initialSortDirection ?? 'asc');
        this.sortBy = signal(config.initialSortBy ?? 'id');
    }

    fetch() {
        const query: PageParameters = {
            page: this.page(),
            size: this.pageSize(),
            sortBy: this.sortBy(),
            sortDirection: this.sortDirection(),
        };

        const obs = this.searchTerm()
            ? this.fetchByQuery(this.searchTerm(), query)
            : this.fetchAll(query);

        obs.subscribe((result) => {
            this.items.set(result.content);
            this.totalPages.set(result.page.totalPages);
            this.page.set(result.page.number);
        });
    }

    onSort(field: string) {
        if (this.sortBy() === field) {
            this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
        } else {
            this.sortBy.set(field);
            this.sortDirection.set('asc');
        }
        this.page.set(0);
        this.fetch();
    }

    onPageChange(newPage: number) {
        this.page.set(newPage);
        this.fetch();
    }

    onPageSizeChange(newSize: number) {
        this.pageSize.set(newSize);
        this.page.set(0);
        this.fetch();
    }

    onSearch() {
        this.page.set(0);
        this.fetch();
    }

    onClearSearch() {
        this.searchTerm.set('');
        this.page.set(0);
        this.fetch();
    }
}