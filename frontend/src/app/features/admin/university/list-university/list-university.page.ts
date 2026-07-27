import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { UniversityService } from '../university.service';
import { University } from '../../../../shared/types/university.type';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-list-university',
  imports: [PaginationComponent, ConfirmDialogComponent],
  templateUrl: './list-university.page.html',
})
export class ListUniversityPage {

  confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

  universityService = inject(UniversityService);
  router = inject(Router);

  universities = signal<Array<University>>([]);

  totalPages = signal(0);
  page = signal(0);
  pageSize = signal(10);
  sortBy = signal('name');
  sortDirection = signal<'asc' | 'desc'>('asc');
  searchTerm = signal('');


  ngOnInit() {
    this.fetchUniversities();
  }

  fetchUniversities() {
    this.universityService.getUniversities({
      page: this.page(),
      size: this.pageSize(),
      sortBy: this.sortBy(),
      sortDirection: this.sortDirection()
    }).subscribe((page) => {
      this.universities.set(page.content);
      this.totalPages.set(page.page.totalPages);
      this.page.set(page.page.number);
    });
  }

  fetchUniversitiesByName() {
    this.universityService.getUniversitiesByName(this.searchTerm(), {
      page: this.page(),
      size: this.pageSize(),
      sortBy: this.sortBy(),
      sortDirection: this.sortDirection()
    }).subscribe((page) => {
      this.universities.set(page.content);
      this.totalPages.set(page.page.totalPages);
      this.page.set(page.page.number);
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
    this.fetchUniversities();
  }

  onPageChange(newPage: number) {
    this.page.set(newPage);
    this.fetchUniversities();
  }

  onPageSizeChange(newSize: number) {
    this.pageSize.set(newSize);
    this.page.set(0);
    this.searchTerm() ? this.fetchUniversitiesByName() : this.fetchUniversities();
  }

  onSearch() {
    this.page.set(0);
    this.fetchUniversitiesByName();
  }

  onClearSearch() {
    this.searchTerm.set('');
    this.page.set(0);
    this.fetchUniversities();
  }

  onEdit(university: University) {
    this.router.navigate(['/admin/universities', university.id, 'edit']);
  }

  onDeleteClick(university: University) {
    this.confirmDialog.set({
      open: true,
      title: 'Delete university',
      message: `Are you sure you want to delete ${university.name}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteUniversity(university),
    });
  }


  onCreate() {
    this.router.navigate(['/admin/universities/create']);
  }

  onConfirmDialogConfirm() {
    this.confirmDialog().action?.();
    this.closeConfirmDialog();
  }

  onConfirmDialogCancel() {
    this.closeConfirmDialog();
  }

  private closeConfirmDialog() {
    this.confirmDialog.set(CLOSED_DIALOG);
  }

  private deleteUniversity(university: University) {
    this.universityService.deleteUniversity(university.id).subscribe(() => {
      this.fetchUniversities();
    });
  }

}
