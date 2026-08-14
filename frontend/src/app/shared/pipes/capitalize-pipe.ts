import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'capitalize',
})
export class CapitalizePipe implements PipeTransform {

  transform(value: unknown, ...args: unknown[]): unknown {
    const str = String(value).toLocaleLowerCase();
    return str.charAt(0).toUpperCase() + str.slice(1);
  }
}
