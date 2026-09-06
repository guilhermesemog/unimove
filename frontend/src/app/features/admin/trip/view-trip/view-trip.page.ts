import { Component, inject, Input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { UiStateComponent } from '../../../../shared/components/ui-state/ui-state.component';
import { TripService } from '../trip.service';

@Component({
  selector: 'app-view-trip-page',
  imports: [UiStateComponent],
  templateUrl: './view-trip.page.html',
})
export class ViewTripPage {
  @Input() id!: string;

  private readonly tripService = inject(TripService);
  private readonly router = inject(Router);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.tripService.getTripById(this.id).subscribe({
      next: (trip) => this.router.navigate(
        ['/admin/interest-lists', trip.interestList.id, 'view'],
        { replaceUrl: true },
      ),
      error: () => this.error.set('We could not find this trip.'),
    });
  }
}
