import { Component, input, output } from '@angular/core';

import { InterestList } from '../../types/interest-list.type';
import { DateLabelPipe } from '../../pipes/date-label.pipe';
import { TimeLabelPipe } from '../../pipes/time-label.pipe';
import { RouteTimelineComponent } from '../route-timeline/route-timeline.component';
import { StatusChipComponent } from '../status-chip/status-chip.component';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-available-trip-card',
  standalone: true,
  imports: [DateLabelPipe, TimeLabelPipe, RouteTimelineComponent, StatusChipComponent, Icon],
  templateUrl: './available-trip-card.component.html',
})
export class AvailableTripCardComponent {
  readonly trip = input.required<InterestList>();
  readonly origin = input('Your boarding stop');
  readonly booked = input(false);
  readonly selected = output<void>();
}
