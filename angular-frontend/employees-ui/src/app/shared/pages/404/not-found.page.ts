import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-page-not-found',
    templateUrl: './not-found.page.html',
    imports: [RouterLink, TranslatePipe],
})
export class NotFoundPage {}
