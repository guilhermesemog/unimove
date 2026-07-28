import { Component, inject } from '@angular/core';
import { BoardingStopService } from '../boarding-stop.service';
import { Router } from '@angular/router';
import { BoardingStop } from '../../../../shared/types/boarding-stop.type';
import { ListState } from '../../../../shared/utils/list-state';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { SearchBarComponent } from '../../../../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-list-boarding-stop',
  imports: [PaginationComponent, ConfirmDialogComponent, DataTableComponent, HeaderComponent, SearchBarComponent],
  templateUrl: './list-boarding-stop.page.html',
})
export class ListBoardingStopPage {
  boardingStopService = inject(BoardingStopService);
  router = inject(Router);

  list = new ListState<BoardingStop>({
    initialSortBy: 'local',
    fetchAll: (q) => this.boardingStopService.getBoardingStops(q),
    fetchByQuery: (term, q) => this.boardingStopService.getBoardingStopsByLocal(term, q),
  });

  confirmDialog = new ConfirmDialogController();

  columns: TableColumn<BoardingStop>[] = [
    { key: 'local', label: 'Local', sortable: true },
  ];

  ngOnInit() {
    this.list.fetch();
  }

  onEdit(boardingStop: BoardingStop) {
    this.router.navigate(['/admin/boarding-stops', boardingStop.id, 'edit']);
  }

  onDeleteClick(boardingStop: BoardingStop) {
    this.confirmDialog.open({
      title: 'Delete boarding stop',
      message: `Are you sure you want to delete ${boardingStop.local}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      variant: 'danger',
      action: () => this.deleteBoardingStop(boardingStop),
    });
  }

  onCreate() {
    this.router.navigate(['/admin/boarding-stops/create']);
  }

  private deleteBoardingStop(boardingStop: BoardingStop) {
    this.boardingStopService.deleteBoardingStop(boardingStop.id).subscribe(() => {
      this.list.fetch();
    });
  }
}
