import { Component, inject } from '@angular/core';
import { InterestListService } from '../admin/interest-list/interest-list.service';

@Component({
  selector: 'app-interest-list.page',
  imports: [],
  templateUrl: './interest-list.page.html',
})
export class InterestListPage { 
  interestListService = inject(InterestListService);

  

}
