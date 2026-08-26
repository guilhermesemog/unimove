import { Component, computed, input, output } from '@angular/core';

import { UI_COPY } from '../../../core/content/ui-copy';
import { Icon } from '../icon/icon';

export type UiStateKind = 'loading' | 'empty' | 'error';

@Component({
  selector: 'app-ui-state',
  standalone: true,
  imports: [Icon],
  templateUrl: './ui-state.component.html',
})
export class UiStateComponent {
  readonly kind = input<UiStateKind>('empty');
  readonly title = input<string>();
  readonly description = input<string>();
  readonly compact = input(false);
  readonly retry = output<void>();

  protected readonly copy = UI_COPY;
  protected readonly resolvedTitle = computed(() => this.title() ?? this.copy.states[this.kind()]);
  protected readonly resolvedDescription = computed(() =>
    this.description() ?? this.copy.states[`${this.kind()}Description` as const]
  );
  protected readonly icon = computed(() => ({ loading: 'progress_activity', empty: 'inbox', error: 'error_outline' })[this.kind()]);
}
