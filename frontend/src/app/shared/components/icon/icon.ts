import { Component, computed, input } from '@angular/core';

export type IconSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-icon',
  imports: [],
  templateUrl: './icon.html',
})
export class Icon {
  readonly iconName = input.required<string>();
  readonly size = input<IconSize>('md');
  readonly spin = input(false);
  readonly label = input<string>();

  protected readonly sizeClasses = computed(() => ({
    sm: 'h-4 w-4 text-[1rem]',
    md: 'h-5 w-5 text-[1.25rem]',
    lg: 'h-6 w-6 text-[1.5rem]',
  })[this.size()]);
}
