import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';

@Component({
    selector: 'app-back-button',
    standalone: true,
    templateUrl: './back-button.component.html',
})
export class BackButtonComponent {
    private location = inject(Location);

    goBack() {
        this.location.back();
    }
}