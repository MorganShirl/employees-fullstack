import { Injectable, signal } from '@angular/core';

export interface AuthState {
    isAuthenticated: boolean;
    username: string | null;
    email: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
    private readonly _authState = signal<AuthState>({
        isAuthenticated: false,
        username: null,
        email: null
    });

    authState = this._authState.asReadonly();

    login(username: string, email: string) {
        this._authState.set({
            isAuthenticated: true,
            username,
            email
        });
    }

    logout() {
        this._authState.set({
            isAuthenticated: false,
            username: null,
            email: null
        });
    }
}
