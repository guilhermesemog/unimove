import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { ErrorDialogService } from '../error-dialog/error-dialog.service';

interface BackendErrorBody {
    message: string;
    details: string;
    timestamp: string;
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const errorDialogService = inject(ErrorDialogService);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            const body = error.error as BackendErrorBody;
            switch (error.status) {
                case 401:
                    if (body.message === 'JWT token has expired') {
                        errorDialogService.show(
                            'Session expired',
                            'Your session has expired. Please log in again to continue.'
                        );
                    }

                    if (body.message === 'Authentication failed') {
                        errorDialogService.show(
                            body.message ?? 'Wrong credentials',
                            body.details ?? 'Your credentials are invalid. Please check them and try again.'
                        );
                    }

                    authService.logout();
                    break;

                case 403:
                    errorDialogService.show(
                        'Access denied',
                        'You are not authorized to perform this action. Please log in or check your permissions and try again.'
                    );
                    break;

                default:
                    errorDialogService.show(
                        body.message ?? 'Server error',
                        body.details ?? 'An unexpected error occurred. Please try again later.'
                    );
                    break;
            }

            return throwError(() => error);
        })
    );
};