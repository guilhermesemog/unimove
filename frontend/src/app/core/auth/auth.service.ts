import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment.development';
import { User, UserRole } from '../../shared/types/user.type';
import { firstValueFrom } from 'rxjs/internal/firstValueFrom';

const API_BASE_URL = environment.apiUrl;

interface LoginResponse {
    accessToken: string;
}

@Service()
export class AuthService {
    http = inject(HttpClient);
    router = inject(Router);

    async login(cpf: string, password: string): Promise<void> {
        const response = await firstValueFrom(
            this.http.post<LoginResponse>(`${API_BASE_URL}/auth/login`, { cpf, password })
        );

        localStorage.setItem('accessToken', response.accessToken);

        await this.identify().then(async (user) => {
            if (user.role === UserRole.Admin) {
                this.router.navigate(['/admin']);
                return;
            }

            this.router.navigate(['/']);
        });
    }

    async identify(): Promise<User> {
        const user = await firstValueFrom(
            this.http.get<User>(`${API_BASE_URL}/users/me`)
        );

        return user;
    }

    logout(): void {
        localStorage.removeItem('accessToken');
        // window.location.href > router.navigate
        window.location.href = '/login';
    }
}