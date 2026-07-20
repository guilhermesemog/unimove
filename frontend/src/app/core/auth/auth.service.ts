import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment.development';
import { User, UserProfile, UserRole } from '../../shared/types/user.type';
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

        await this.identify();

        if (this.getRole() === UserRole.Admin) {
            await this.router.navigate(['/admin']);
            return;
        }

        await this.router.navigate(['/']);
    }

    async identify(): Promise<User> {
        const user = await firstValueFrom(
            this.http.get<User>(`${API_BASE_URL}/users/me`)
        );

        const profile: UserProfile = { id: user.id, role: user.role, firstName: user.firstName, lastName: user.lastName };
        localStorage.setItem('userProfile', JSON.stringify(profile));

        return user;
    }

    logout(): void {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userProfile');

        this.router.navigate(['/login']);
    }

    getRole(): UserRole | null {
        const profileString = localStorage.getItem('userProfile');
        console.log('profileString', profileString);
        if (!profileString) {
            return null;
        }

        const profile: UserProfile = JSON.parse(profileString);
        return profile.role;
    }

    getFirstName(): string | null {
        const profileString = localStorage.getItem('userProfile');
        if (!profileString) {
            return null;
        }

        const profile: UserProfile = JSON.parse(profileString);
        return profile.firstName;
    }

    getLastName(): string | null {
        const profileString = localStorage.getItem('userProfile');
        if (!profileString) {
            return null;
        }

        const profile: UserProfile = JSON.parse(profileString);
        return profile.lastName;
    }
}