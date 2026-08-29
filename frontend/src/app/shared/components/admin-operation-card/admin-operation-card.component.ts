import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AdminOperation } from '../../types/admin-operation.type';
import { operationAttention, operationStage } from '../../utils/admin-operation';
import { DateLabelPipe } from '../../pipes/date-label.pipe';
import { TimeLabelPipe } from '../../pipes/time-label.pipe';
import { Icon } from '../icon/icon';
import { OccupancyMeterComponent } from '../occupancy-meter/occupancy-meter.component';
import { StatusChipComponent } from '../status-chip/status-chip.component';

@Component({
  selector: 'app-admin-operation-card',
  standalone: true,
  imports: [DateLabelPipe, Icon, OccupancyMeterComponent, RouterLink, StatusChipComponent, TimeLabelPipe],
  templateUrl: './admin-operation-card.component.html',
})
export class AdminOperationCardComponent {
  readonly operation = input.required<AdminOperation>();

  protected readonly attention = computed(() => operationAttention(this.operation()));
  protected readonly stage = computed(() => operationStage(this.operation()));
}
