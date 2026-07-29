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
    const router = inject(Router);
    const authService = inject(AuthService);
    const errorDialogService = inject(ErrorDialogService);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            switch (error.status) {
                case 401:
                    const body = error.error as BackendErrorBody;
                    errorDialogService.show(
                        body.message ?? 'The request could not be completed. Please check the form and try again.',
                        body.details ?? 'You are not authorized to perform this action. Please log in and try again.'
                    );
                    authService.logout();
                    break;

                case 403:
                    authService.logout();
                    break;

                case 400:
                case 409:
                case 422: {
                    const body = error.error as BackendErrorBody;
                    errorDialogService.show(
                        'Request error',
                        body.message ?? 'The request could not be completed. Please check the form and try again.'
                    );
                    break;
                }

                case 500:
                default:
                    errorDialogService.show(
                        'Something went wrong',
                        'An unexpected error occurred. Please try again later.'
                    );
                    break;
            }

            return throwError(() => error);
        })
    );
};