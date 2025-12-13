import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { AuthApiService } from '@core/services/auth-api.service';
import { AuthStore } from '@core/store/auth.store';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss',
    imports: [
        RouterOutlet,
        TranslatePipe,
        MatToolbarModule,
        MatButtonModule
    ]
})
export class AppComponent {
    private readonly translateService = inject(TranslateService);
    private readonly authApiService = inject(AuthApiService);
    private readonly authStore = inject(AuthStore);
    private readonly router = inject(Router);

    switchLang(lang: 'en' | 'fr') {
        this.translateService.use(lang);
    }

    isCurrentLang(lang: 'en' | 'fr') {
        return this.translateService.getCurrentLang() === lang;
    }

    isLoggedIn() {
        return this.authStore.authState().isAuthenticated;
    }

    username() {
        return this.authStore.authState().username;
    }

    email() {
        return this.authStore.authState().email;
    }

    logout() {
        this.authApiService.logout()
            .pipe(
                finalize(() => {
                    this.router.navigateByUrl('/login');
                })
            )
            .subscribe();
    }
}
