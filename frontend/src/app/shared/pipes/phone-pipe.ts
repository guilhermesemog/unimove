import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'phone',
})
export class PhonePipe implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {
    if (typeof value !== 'string') {
      return value;
    }

    const cleanedValue = value.replace(/\D/g, '');

    if (cleanedValue.length !== 12) {
      return value;
    }

    const formattedValue = cleanedValue.replace(
      /(\d{2})(\d{1})(\d{4})(\d{4})/,
      '($1) $2 $3-$4'
    );

    return formattedValue;
  }
}
