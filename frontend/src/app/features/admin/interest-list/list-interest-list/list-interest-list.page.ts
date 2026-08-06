import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { InterestListService } from '../interest-list.service';
import { InterestList } from '../../../../shared/types/interest-list.type';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { ListState } from '../../../../shared/utils/list-state';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';

@Component({
  selector: 'app-list-interest-list.page',
  imports: [CommonModule, DataTableComponent, HeaderComponent, PaginationComponent, ConfirmDialogComponent],
  templateUrl: './list-interest-list.page.html'
})
export class ListInterestListPage {

  interestListService = inject(InterestListService);
  router = inject(Router);

  list = new ListState<InterestList>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.interestListService.getInterestLists(q),
    fetchByQuery: (term, q) => this.interestListService.getInterestLists(q),
  });

  confirmDialog = new ConfirmDialogController();

  columns: TableColumn<InterestList>[] = [
    { key: 'id', label: 'ID', sortable: true, align: 'center' },
    { key: 'referenceDate', label: 'Reference Date', sortable: true },
    { key: 'closingTime', label: 'Closing Time', sortable: true },
    { key: 'departureTime', label: 'Departure Time', sortable: true },
    { key: 'arrivalTime', label: 'Arrival Time', sortable: true },
    { key: 'returnDepartureTime', label: 'Return Departure Time', sortable: true },
    { key: 'returnArrivalTime', label: 'Return Arrival Time', sortable: true },
    { key: 'destinationId', label: 'Destination ID', sortable: true, align: 'center' },
    { key: 'listStatus', label: 'List Status', sortable: true }
  ]

  ngOnInit() {
    this.list.fetch();
  }

  onEdit(interestList: InterestList) {
    this.router.navigate(['/admin/interest-lists', interestList.id, 'edit']);
  }

  onDelete(interestList: InterestList) {
    this.confirmDialog.open({
      title: 'Delete Interest List',
      message: `Are you sure you want to delete the interest list with reference date ${interestList.referenceDate}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteInterestList(interestList),
    });
  }

  onCreate() {
    this.router.navigate(['/admin/interest-lists/create']);
  }

  onView(interestList: InterestList) {
    this.router.navigate(['/admin/interest-lists', interestList.id, 'view']);
  }

  private deleteInterestList(interestList: InterestList) {
    this.interestListService.deleteInterestList(interestList.id).subscribe(() => {
      this.list.fetch()
    });
  }
}
