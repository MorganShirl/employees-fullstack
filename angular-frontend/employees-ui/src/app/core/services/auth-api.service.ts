import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { finalize, tap } from 'rxjs';
import { AuthStore } from '@core/store/auth.store';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    username: string;
    email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
    private readonly baseUrl = environment.apiBaseUrl;
    private readonly authStore = inject(AuthStore);

    constructor(private readonly httpClient: HttpClient) {}

    login(payload: LoginRequest) {
        return this.httpClient.post<LoginResponse>(
            `${this.baseUrl}/auth/login`,
            payload,
            { withCredentials: true } // Doesn't hurt if same origin, with CORS it allows the browser to store/send JSESSIONID cookie
        ).pipe(
            tap(response => {
                this.authStore.login(response.username, response.email);
            })
        );
    }

    getCurrentUser() {
        return this.httpClient.get<LoginResponse>(
            `${this.baseUrl}/auth/current-user`,
            { withCredentials: true } // send JSESSIONID cookie
        );
    }

    logout() {
        return this.httpClient.post<void>(
            `${this.baseUrl}/auth/logout`,
            {},
            { withCredentials: true } // CORS: send JSESSIONID so backend can terminate session
        ).pipe(
            finalize(() => {
                this.authStore.logout();   // central place to clear client auth state
            })
        );
    }
}
