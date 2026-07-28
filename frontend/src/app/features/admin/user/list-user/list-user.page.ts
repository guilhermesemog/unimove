import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserService } from '../user.service';
import { User, UserRole } from '../../../../shared/types/user.type';
import { CpfPipe } from '../../../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../../../shared/pipes/phone-pipe';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AuthService } from '../../../../core/auth/auth.service';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { SearchBarComponent } from '../../../../shared/components/search-bar/search-bar.component';
import { ListState } from '../../../../shared/utils/list-state';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';

@Component({
  selector: 'app-list-user',
  imports: [DataTableComponent, HeaderComponent, SearchBarComponent, CommonModule, PaginationComponent, ConfirmDialogComponent],
  templateUrl: './list-user.page.html',
  providers: [CpfPipe, PhonePipe],
})
export class ListUserPage {

  authService = inject(AuthService);
  userService = inject(UserService);
  router = inject(Router);

  constructor(private cpfPipe: CpfPipe, private phonePipe: PhonePipe) { }

  list = new ListState<User>({
    initialSortBy: 'firstName',
    fetchAll: (q) => this.userService.getUsers(q),
    fetchByQuery: (term, q) => this.userService.getUsersByFullName(term, q),
  });

  confirmDialog = new ConfirmDialogController();

  columns: TableColumn<User>[] = [
    { key: 'firstName', label: 'First Name', sortable: true },
    { key: 'lastName', label: 'Last Name', sortable: true },
    { key: 'cpf', label: 'CPF', sortable: true, format: (user) => this.cpfPipe.transform(user.cpf) as string },
    { key: 'phone', label: 'Phone', sortable: true, format: (user) => this.phonePipe.transform(user.phone) as string },
    { key: 'role', label: 'Role', sortable: true },
    { key: 'active', label: 'Active', sortable: true, format: (user) => user.active ? 'Yes' : 'No' },
  ]

  ngOnInit() {
    this.list.fetch();
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
    this.confirmDialog.open({
      title: 'Delete user',
      message: `Are you sure you want to delete ${user.firstName} ${user.lastName}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteUser(user),
    });
  }

  onDeactivateClick(user: User) {
    this.confirmDialog.open({
      title: 'Deactivate user',
      message: `Are you sure you want to deactivate ${user.firstName} ${user.lastName}? They will lose access to the system.`,
      confirmLabel: 'Deactivate',
      variant: 'danger',
      action: () => this.toggleUserStatus(user),
    });
  }

  onActivateClick(user: User) {
    this.confirmDialog.open({
      title: 'Activate user',
      message: `Activate ${user.firstName} ${user.lastName}? They will regain access to the system.`,
      confirmLabel: 'Activate',
      variant: 'default',
      action: () => this.toggleUserStatus(user),
    });
  }

  onCreate() {
    this.router.navigate(['/admin/users/create']);
  }

  private deleteUser(user: User) {
    this.userService.deleteUser(user.id).subscribe(() => {
      this.list.fetch();
    });
  }

  private toggleUserStatus(user: User) {
    this.userService.toggleUserActiveStatus(user.id).subscribe(() => {
      this.list.fetch();
    });
  }
}