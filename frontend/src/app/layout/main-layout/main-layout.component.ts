import { Component } from '@angular/core';
import { Sidebar } from '../sidebar/sidebar.component';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-main-layout',
  imports: [Sidebar, RouterOutlet],
  templateUrl: './main-layout.component.html',
})
export class MainLayout { }
