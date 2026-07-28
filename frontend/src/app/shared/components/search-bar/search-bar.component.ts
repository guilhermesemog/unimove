// shared/search-bar/search-bar.component.ts
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
})
export class SearchBarComponent {
  value = input.required<string>();
  placeholder = input<string>('Search...');

  valueChange = output<string>();
  search = output<void>();
  clear = output<void>();

  onInput(val: string) {
    this.valueChange.emit(val);
  }
}