import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { buildProblemDetailLines, toProblemDetail } from '@shared/utils/error.util';
import { inject } from '@angular/core';
import { NotificationService } from '@core/services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {

    const notificationService = inject(NotificationService);

    return next(req).pipe(
        catchError((err: unknown) => {
            if (err instanceof HttpErrorResponse) {

                const isSessionCheck = req.url.endsWith('/current-user') &&
                    (err.status === 403);
                if (isSessionCheck) {
                    // Expected: user is simply not logged in on startup
                    return throwError(() => err);
                }

                const problemDetail = toProblemDetail(err);

                const wrapped = new HttpErrorResponse({
                    ...err,
                    url: err.url ?? undefined,
                    error: problemDetail,
                });

                console.error('[Error-interceptor_HTTP]', req.method, req.url, wrapped);

                const lines = buildProblemDetailLines(problemDetail);
                notificationService.showStickyErrorLines(lines);

                return throwError(() => wrapped);
            }

            console.error('[Error-interceptor_NON-HTTP]', req, err);
            notificationService.showStickyError('Unexpected client-side error.');

            return throwError(() => err);
        })
    );
}
