import { Component, input } from '@angular/core';

import { UI_COPY } from '../../../core/content/ui-copy';

@Component({
  selector: 'app-brand',
  standalone: true,
  templateUrl: './brand.component.html',
})
export class BrandComponent {
  readonly compact = input(false);
  readonly showTagline = input(false);
  protected readonly copy = UI_COPY.brand;
}
