import { Component, computed, input } from '@angular/core';

import { TripType } from '../../types/booking.type';
import { Icon } from '../icon/icon';
import { TimeLabelPipe } from '../../pipes/time-label.pipe';

@Component({
  selector: 'app-route-timeline',
  standalone: true,
  imports: [Icon, TimeLabelPipe],
  templateUrl: './route-timeline.component.html',
})
export class RouteTimelineComponent {
  readonly origin = input('Your boarding stop');
  readonly destination = input.required<string>();
  readonly departureTime = input.required<string>();
  readonly arrivalTime = input.required<string>();
  readonly returnDepartureTime = input.required<string>();
  readonly returnArrivalTime = input.required<string>();
  readonly tripType = input<TripType | null>(null);
  readonly compact = input(false);

  protected readonly showOutbound = computed(() => this.tripType() !== TripType.INBOUND);
  protected readonly showReturn = computed(() => this.tripType() !== TripType.OUTBOUND);
}
