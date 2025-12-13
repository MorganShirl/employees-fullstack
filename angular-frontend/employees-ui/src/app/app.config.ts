import {
    ApplicationConfig,
    importProvidersFrom,
    inject,
    provideAppInitializer,
    provideBrowserGlobalErrorListeners,
    provideZonelessChangeDetection
} from '@angular/core';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { PreloadAllModules, provideRouter, TitleStrategy, withInMemoryScrolling, withPreloading, withViewTransitions } from '@angular/router';
import { routes } from './app.routes';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { I18nTitleStrategy } from '@core/i18n/i18n-title-strategy';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from '@core/interceptors/error.interceptor';
import { xsrfInterceptor } from '@core/interceptors/xsrf.interceptor';
import { AuthApiService } from '@core/services/auth-api.service';
import { AuthStore } from '@core/store/auth.store';
import { catchError, firstValueFrom, of, tap } from 'rxjs';

export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideZonelessChangeDetection(),

        importProvidersFrom(MatSnackBarModule),

        provideHttpClient(
            withFetch(),
            withInterceptors([errorInterceptor, xsrfInterceptor])
        ),

        provideRouter(
            routes,
            withViewTransitions(),
            withInMemoryScrolling({
                anchorScrolling: 'enabled',
                scrollPositionRestoration: 'enabled'
            }),
            withPreloading(PreloadAllModules)
        ),

        { provide: TitleStrategy, useClass: I18nTitleStrategy },

        provideTranslateService({
            // loader for /assets/i18n/{lang}.json
            loader: provideTranslateHttpLoader({
                prefix: '/assets/i18n/',
                suffix: '.json',
                // optionally:
                // enforceLoading: false,
                // useHttpBackend: false
            }),
            fallbackLang: 'en',
            lang: 'en', // initial language
        }),

        // On app startup, check if a server-side session exists and restore the auth state before bootstrapping.
        provideAppInitializer(() => {
            const authApiService = inject(AuthApiService);
            const authStore = inject(AuthStore);

            return firstValueFrom(
                authApiService.getCurrentUser().pipe(
                    tap(response => {
                        // Session is valid -> restore signal state
                        authStore.login(response.username, response.email);
                    }),
                    catchError(() => {
                        // No valid session -> ensure logged-out state
                        authStore.logout();
                        return of(null);
                    })
                )
            );
        })
    ]
};
