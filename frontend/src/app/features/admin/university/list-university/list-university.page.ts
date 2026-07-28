import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { UniversityService } from '../university.service';
import { University } from '../../../../shared/types/university.type';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { SearchBarComponent } from '../../../../shared/components/search-bar/search-bar.component';
import { ListState } from '../../../../shared/utils/list-state';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';

@Component({
  selector: 'app-list-university',
  imports: [PaginationComponent, ConfirmDialogComponent, DataTableComponent, HeaderComponent, SearchBarComponent],
  templateUrl: './list-university.page.html',
})
export class ListUniversityPage {
  universityService = inject(UniversityService);
  router = inject(Router);

  list = new ListState<University>({
    initialSortBy: 'name',
    fetchAll: (q) => this.universityService.getUniversities(q),
    fetchByQuery: (term, q) => this.universityService.getUniversitiesByName(term, q),
  });

  confirmDialog = new ConfirmDialogController();

  columns: TableColumn<University>[] = [
    { key: 'name', label: 'Name', sortable: true },
    { key: 'address', label: 'Address', sortable: true },
  ];

  ngOnInit() {
    this.list.fetch();
  }

  onEdit(university: University) {
    this.router.navigate(['/admin/universities', university.id, 'edit']);
  }

  onDeleteClick(university: University) {
    this.confirmDialog.open({
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

  private deleteUniversity(university: University) {
    this.universityService.deleteUniversity(university.id).subscribe(() => {
      this.list.fetch();
    });
  }
}