import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '@core/services/auth-api.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';
import { MatCard } from '@angular/material/card';
import { finalize } from 'rxjs';

@Component({
    standalone: true,
    selector: 'app-login',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslatePipe,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatCard
    ],
    templateUrl: './login.page.html'
})
export class LoginPage {
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly authApiService = inject(AuthApiService);
    private readonly formBuilder = inject(NonNullableFormBuilder);

    readonly loginForm = this.formBuilder.group({
        username: this.formBuilder.control<string>('', [Validators.required]),
        password: this.formBuilder.control<string>('', [Validators.required])
    });

    readonly loading = signal(false);
    readonly error = signal<string | null>(null);

    submit() {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        this.error.set(null);

        const credentials = this.loginForm.getRawValue();

        this.authApiService.login(credentials)
            .pipe(
                finalize(() => {
                    this.loading.set(false);
                })
            ).subscribe({
                next: () => {
                    const returnUrl =
                        this.route.snapshot.queryParamMap.get('returnUrl') || '/home';
                    this.router.navigateByUrl(returnUrl);
                },
                error: err => {
                    if (err.status === 401) {
                        this.error.set('login.msg');
                    } else {
                        this.error.set('Login failed, please try again');
                    }
                }
        });
    }
}
