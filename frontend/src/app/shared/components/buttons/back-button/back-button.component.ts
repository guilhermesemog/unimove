import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { UI_COPY } from '../../../../core/content/ui-copy';
import { Icon } from '../../icon/icon';

@Component({
    selector: 'app-back-button',
    standalone: true,
    imports: [Icon],
    templateUrl: './back-button.component.html',
})
export class BackButtonComponent {
    private location = inject(Location);
    protected readonly copy = UI_COPY;

    goBack() {
        this.location.back();
    }
}
