import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

import { User, UserCreateRequest, UserUpdateRequest } from '../../../shared/types/user.type';
import { PageResponse, PageParameters } from '../../../shared/types/page.type';

import { environment } from '../../../../environments/environment.development';
const API_BASE_URL = environment.apiUrl;

@Service()
export class UserService {
    private http = inject(HttpClient);

    getUsers(params: PageParameters): Observable<PageResponse<User>> {
        return this.http.get<PageResponse<User>>(`${API_BASE_URL}/users`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection
            }
        });
    }

    getUserById(userId: number): Observable<User> {
        return this.http.get<User>(`${API_BASE_URL}/users/${userId}`);
    }

    getUsersByFullName(fullName: string, params: PageParameters): Observable<PageResponse<User>> {
        return this.http.get<PageResponse<User>>(`${API_BASE_URL}/users/search`, {
            params: {
                page: params.page.toString(),
                size: params.size.toString(),
                sortBy: params.sortBy,
                sortDirection: params.sortDirection,
                fullName: fullName
            }
        });
    }

    updateUser(userId: number, user: User): Observable<void> {
        const updatableUser: UserUpdateRequest = {
            cpf: user.cpf,
            firstName: user.firstName,
            lastName: user.lastName,
            phone: user.phone
        };

        return this.http.put<void>(`${API_BASE_URL}/users/${userId}`, updatableUser);
    }

    createUser(user: UserCreateRequest): Observable<User> {
        return this.http.post<User>(`${API_BASE_URL}/users`, user);
    }

    deleteUser(userId: number): Observable<void> {
        return this.http.delete<void>(`${API_BASE_URL}/users/${userId}`);
    }

    toggleUserActiveStatus(userId: number): Observable<void> {
        return this.http.patch<void>(`${API_BASE_URL}/users/${userId}/status`, {});
    }

}
