import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthStore } from '@core/store/auth.store';

// _route: unused but required parameter
export const authGuard: CanActivateFn = (_route, state): boolean | UrlTree => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    const isAuthenticated = authStore.authState().isAuthenticated;
    const isLoginUrl = state.url.startsWith('/login');

    // 1) Not authenticated and trying to access a protected page => go to /login
    if (!isAuthenticated && !isLoginUrl) {
        return router.createUrlTree(
            ['/login'],
            { queryParams: { returnUrl: state.url } } // to redirect to the url in the address bar, after the user logs in
        );
    }

    // 2) Authenticated and trying to access /login => go to /home
    if (isAuthenticated && isLoginUrl) {
        return router.createUrlTree(['/home']);
    }

    // 3) All other cases: allow navigation
    return true;
};
