import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '@env/environment';
import { getCookie } from '@shared/utils/cookie.util';

const XSRF_HEADER_NAME = 'X-XSRF-TOKEN';
const XSRF_COOKIE_NAME = 'XSRF-TOKEN';

const MUTATING_HTTP_METHODS = new Set([
    'POST',
    'PUT',
    'PATCH',
    'DELETE'
]);

// Normalize apiBaseUrl once (remove trailing slash if present)
const API_BASE_URL = environment.apiBaseUrl.replace(/\/+$/, '');

const isMutatingRequest = (method: string): boolean =>
    MUTATING_HTTP_METHODS.has(method);

// Only send XSRF header to *our* backend, defined by apiBaseUrl
const isApiRequest = (url: string): boolean =>
    url.startsWith(API_BASE_URL + '/');

export const xsrfInterceptor: HttpInterceptorFn = (req, next) => {
    if (!isMutatingRequest(req.method)) {
        return next(req);
    }

    if (!isApiRequest(req.url)) {
        return next(req);
    }

    // do not override if already set for some reason
    if (req.headers.has(XSRF_HEADER_NAME)) {
        return next(req);
    }

    const token = getCookie(XSRF_COOKIE_NAME);
    if (!token) {
        return next(req);
    }

    return next(
        req.clone({
            setHeaders: {
                [XSRF_HEADER_NAME]: token
            }
        })
    );
};
