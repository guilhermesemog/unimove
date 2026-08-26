import { Component, inject, signal } from '@angular/core';

import { Trip } from '../../shared/types/trip.type';
import { TripService } from '../admin/trip/trip.service';
import { ListState } from '../../shared/utils/list-state';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { Router } from '@angular/router';
import { StatusChipComponent } from '../../shared/components/status-chip/status-chip.component';

@Component({
  selector: 'app-trips.page',
  imports: [HeaderComponent, PaginationComponent, StatusChipComponent],
  templateUrl: './trips.page.html',
})
export class TripsPage {

  private tripService = inject(TripService);
  private router = inject(Router);

  list = new ListState<Trip>({
    initialSortBy: 'id',
    initialSortDirection: 'desc',
    fetchAll: (q) => this.tripService.getTripsByUser(q),
    fetchByQuery: (s, q) => this.tripService.getTripsByUser(q)
  }
  );

  ngOnInit() {
    this.list.fetch();
  }

  onViewTrip(trip: Trip) {
    this.router.navigate(['/trips', trip.id]);
  }
}
