import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';

import { Observable } from 'rxjs/internal/Observable';
import { environment } from '../../../environments/environment.development';
import { User, UserRole } from '../../shared/types/user.type';

const API_BASE_URL = environment.apiUrl;

interface LoginResponse {
    accessToken: string;
}

@Service()
export class AuthService {
    http = inject(HttpClient);
    router = inject(Router);

    login(cpf: string, password: string): void {

        this.http.post<LoginResponse>(`${API_BASE_URL}/auth/login`, { cpf, password })
            .subscribe((response) => {
                localStorage.setItem('accessToken', response.accessToken);
                this.identify().subscribe(
                    (user) => {
                        if (user.role === UserRole.Admin) {
                            this.router.navigate(['/admin']);
                            return;
                        }

                        this.router.navigate(['/']);
                    });
            });
    }

    register(cpf: string, password: string, firstName: string, lastName: string, phone: string): void {

        this.http.post<LoginResponse>(`${API_BASE_URL}/auth/register`, { cpf, password, firstName, lastName, phone })
            .subscribe((response) => {
                localStorage.setItem('accessToken', response.accessToken);
                this.identify().subscribe(
                    (user) => {
                        if (user.role === UserRole.Admin) {
                            this.router.navigate(['/admin']);
                            return;
                        }

                        this.router.navigate(['/']);
                    });
            });
    }

    identify(): Observable<User> {
        return this.http.get<User>(`${API_BASE_URL}/users/me`)

    }

    logout(): void {
        localStorage.removeItem('accessToken');
        // window.location.href > router.navigate
        window.location.href = '/login';
    }
}