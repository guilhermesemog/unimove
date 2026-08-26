import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackButtonComponent } from '../buttons/back-button/back-button.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, BackButtonComponent],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  title = input.required<string>();
  description = input<string>('');
  showBack = input(false);
}
