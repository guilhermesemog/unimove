import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ConfirmDialogComponent } from './shared/components/confirm-dialog/confirm-dialog.component';
import { ErrorDialogService } from './core/error-dialog/error-dialog.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ConfirmDialogComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Unimove');

  errorDialogService = inject(ErrorDialogService);
}
