import { Pipe, PipeTransform } from '@angular/core';

import { formatTime } from '../utils/date-time';

@Pipe({ name: 'timeLabel', standalone: true })
export class TimeLabelPipe implements PipeTransform {
  transform(value: string): string {
    return value ? formatTime(value) : '—';
  }
}
