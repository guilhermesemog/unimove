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

    if (cleanedValue.length == 12) {
      return cleanedValue.replace(
        /(\d{3})(\d{1})(\d{4})(\d{4})/,
        '($1) $2 $3-$4'
      );
    }

    if (cleanedValue.length == 11) {
      return cleanedValue.replace(
        /(\d{2})(\d{1})(\d{4})(\d{4})/,
        '($1) $2 $3-$4'
      );
    }

    if (cleanedValue.length == 10) {
      return cleanedValue.replace(
        /(\d{2})(\d{4})(\d{4})/,
        '($1) $2-$3'
      );
    }

    if (cleanedValue.length == 9) {
      return cleanedValue.replace(
        /(\d{1})(\d{4})(\d{4})/,
        '$1 $2-$3'
      );
    }

    return cleanedValue.replace(
      /(\d{4})(\d{4})/,
      '$1-$2'
    );
  }
}
