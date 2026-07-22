import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cpf',
})
export class CpfPipe implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {
    if (typeof value !== 'string') {
      return value;
    }

    const cleanedValue = value.replace(/\D/g, '');

    if (cleanedValue.length !== 11) {
      return value;
    }

    const formattedValue = cleanedValue.replace(
      /(\d{3})(\d{3})(\d{3})(\d{2})/,
      '$1.$2.$3-$4'
    );

    return formattedValue;
  }
}
