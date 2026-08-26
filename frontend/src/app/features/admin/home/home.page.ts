import { Component } from '@angular/core';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { HeaderComponent } from '../../../shared/components/list-header/header.component';

@Component({
  selector: 'app-admin-home',
  imports: [UiStateComponent, HeaderComponent],
  templateUrl: './home.html',
})
export class AdminHomePage {}
