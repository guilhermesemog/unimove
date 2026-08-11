import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'enum',
})
export class EnumPipe implements PipeTransform {

  transform(value: string, ...args: unknown[]): unknown {
    return value.replace('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase())
  }

}
