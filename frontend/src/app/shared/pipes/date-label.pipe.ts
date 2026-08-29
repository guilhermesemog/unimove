import { Pipe, PipeTransform } from '@angular/core';

import { formatDate, relativeDateLabel } from '../utils/date-time';

@Pipe({ name: 'dateLabel', standalone: true })
export class DateLabelPipe implements PipeTransform {
  transform(value: string, relative = false): string {
    if (!value) return '—';
    return relative ? relativeDateLabel(value) : formatDate(value);
  }
}
