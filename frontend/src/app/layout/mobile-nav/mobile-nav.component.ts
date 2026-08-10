import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-mobile-nav',
  imports: [RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './mobile-nav.component.html',
})
export class MobileNavComponent { }