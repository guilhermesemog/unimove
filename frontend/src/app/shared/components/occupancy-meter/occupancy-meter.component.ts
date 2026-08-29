import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-occupancy-meter',
  standalone: true,
  templateUrl: './occupancy-meter.component.html',
})
export class OccupancyMeterComponent {
  readonly used = input.required<number>();
  readonly capacity = input<number | null>(null);
  readonly compact = input(false);

  protected readonly percentage = computed(() => {
    const capacity = this.capacity();
    return capacity && capacity > 0 ? Math.min(100, Math.round((this.used() / capacity) * 100)) : 0;
  });
  protected readonly exceeded = computed(() => this.capacity() !== null && this.used() > this.capacity()!);
}
