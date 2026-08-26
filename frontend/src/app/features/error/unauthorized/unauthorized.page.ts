import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../../shared/components/icon/icon';

@Component({
  selector: 'app-unauthorized',
  imports: [RouterLink, Icon],
  templateUrl: './unauthorized.html',
})
export class UnauthorizedPage {}
