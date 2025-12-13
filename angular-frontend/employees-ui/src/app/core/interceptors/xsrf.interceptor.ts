import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '@env/environment';
import { getCookie } from '@shared/utils/cookie.util';

// Normalize apiBaseUrl once (remove trailing slash if present)
const apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

export const xsrfInterceptor: HttpInterceptorFn = (req, next) => {
    // only care about mutating requests
    const isMutating = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method);
    if (!isMutating) {
        return next(req);
    }

    // Only send XSRF header to *our* backend, defined by apiBaseUrl
    // Works for:
    //   - proxy mode: apiBaseUrl = "/api"  → URLs like "/api/..."
    //   - CORS mode:  apiBaseUrl = "http://host:port/api" → URLs like "http://host:port/api/..."
    const url = req.url;
    const isApiCall =
        url === apiBaseUrl ||
        url.startsWith(apiBaseUrl + '/') ||
        // small extra: if apiBaseUrl is relative, still allow bare "/api" vs "api"
        (apiBaseUrl.startsWith('/') && url.startsWith(apiBaseUrl));

    if (!isApiCall) {
        return next(req);
    }

    // do not override if already set for some reason
    if (req.headers.has('X-XSRF-TOKEN')) {
        return next(req);
    }

    const token = getCookie('XSRF-TOKEN');
    if (!token) {
        return next(req);
    }

    const cloned = req.clone({
        setHeaders: {
            'X-XSRF-TOKEN': token
        }
    });

    return next(cloned);
};
