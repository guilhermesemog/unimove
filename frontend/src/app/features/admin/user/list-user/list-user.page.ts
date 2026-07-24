import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserService } from '../user.service';
import { User, UserRole } from '../../../../shared/types/user.type';
import { CpfPipe } from '../../../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../../../shared/pipes/phone-pipe';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogState, CLOSED_DIALOG } from '../../../../shared/components/confirm-dialog/confirm-dialog.type';
import { AuthService } from '../../../../core/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list-user',
  imports: [CommonModule, CpfPipe, PhonePipe, PaginationComponent, ConfirmDialogComponent],
  templateUrl: './list-user.page.html',
})
export class ListUserPage {
  confirmDialog = signal<ConfirmDialogState>(CLOSED_DIALOG);

  authService = inject(AuthService);
  userService = inject(UserService);
  router = inject(Router);

  users = signal<Array<User>>([]);

  totalPages = signal(0);
  page = signal(0);
  pageSize = signal(10);
  sortBy = signal('firstName');
  sortDirection = signal<'asc' | 'desc'>('asc');
  searchTerm = signal('');


  ngOnInit() {
    this.fetchUsers();
  }

  fetchUsers() {
    this.userService.getUsers({
      page: this.page(),
      size: this.pageSize(),
      sortBy: this.sortBy(),
      sortDirection: this.sortDirection()
    }).subscribe((page) => {
      this.users.set(page.content);
      this.totalPages.set(page.page.totalPages);
      this.page.set(page.page.number);
    });
  }

  fetchUsersByFullName() {
    this.userService.getUsersByFullName(this.searchTerm(), {
      page: this.page(),
      size: this.pageSize(),
      sortBy: this.sortBy(),
      sortDirection: this.sortDirection()
    }).subscribe((page) => {
      this.users.set(page.content);
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
    this.fetchUsers();
  }

  onPageChange(newPage: number) {
    this.page.set(newPage);
    this.fetchUsers();
  }

  onPageSizeChange(newSize: number) {
    this.pageSize.set(newSize);
    this.page.set(0);
    this.searchTerm() ? this.fetchUsersByFullName() : this.fetchUsers();
  }

  onSearch() {
    this.page.set(0);
    this.fetchUsersByFullName();
  }

  onClearSearch() {
    this.searchTerm.set('');
    this.page.set(0);
    this.fetchUsers();
  }

  onEdit(user: User) {
    if (user.role === UserRole.Student) {
      this.router.navigate(['/admin/students', user.id, 'edit']);
    } else if (user.role === UserRole.Conductor) {
      this.router.navigate(['/admin/conductors', user.id, 'edit']);
    } else {
      this.router.navigate(['/admin/users', user.id, 'edit']);
    }
  }


  onDeleteClick(user: User) {
    this.confirmDialog.set({
      open: true,
      title: 'Delete user',
      message: `Are you sure you want to delete ${user.firstName} ${user.lastName}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteUser(user),
    });
  }

  onDeactivateClick(user: User) {
    this.confirmDialog.set({
      open: true,
      title: 'Deactivate user',
      message: `Are you sure you want to deactivate ${user.firstName} ${user.lastName}? They will lose access to the system.`,
      confirmLabel: 'Deactivate',
      variant: 'danger',
      action: () => this.toggleUserStatus(user),
    });
  }

  onActivateClick(user: User) {
    this.confirmDialog.set({
      open: true,
      title: 'Activate user',
      message: `Activate ${user.firstName} ${user.lastName}? They will regain access to the system.`,
      confirmLabel: 'Activate',
      variant: 'default',
      action: () => this.toggleUserStatus(user),
    });
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


  private deleteUser(user: User) {
    this.userService.deleteUser(user.id).subscribe(() => {
      this.fetchUsers();
    });
  }

  private toggleUserStatus(user: User) {
    this.userService.toggleUserActiveStatus(user.id).subscribe(() => {
      this.fetchUsers();
    });
  }
}