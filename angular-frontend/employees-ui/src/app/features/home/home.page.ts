import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-home',
    templateUrl: './home.page.html',
    imports: [RouterLink, TranslatePipe],
})
export class HomePage {}