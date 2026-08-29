import { Component, input, output } from '@angular/core';

import { Booking } from '../../types/booking.type';
import { Trip } from '../../types/trip.type';
import { DateLabelPipe } from '../../pipes/date-label.pipe';
import { RouteTimelineComponent } from '../route-timeline/route-timeline.component';
import { StatusChipComponent } from '../status-chip/status-chip.component';

@Component({
  selector: 'app-journey-card',
  standalone: true,
  imports: [DateLabelPipe, RouteTimelineComponent, StatusChipComponent],
  templateUrl: './journey-card.component.html',
})
export class JourneyCardComponent {
  readonly booking = input.required<Booking>();
  readonly trip = input<Trip | null>(null);
  readonly compact = input(false);
  readonly canCancel = input(false);
  readonly cancelBooking = output<void>();
}
