import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { parseId } from '@shared/utils/id.util';
import { Employee } from '@shared/models/employee.model';
import { TranslatePipe } from '@ngx-translate/core';
import { EmployeeApiService } from '@core/services/employee-api.service';
import { getProblemDetailFromError } from '@shared/utils/error.util';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';

type CreateEmployeeDto = Pick<Employee, 'firstName' | 'lastName' | 'role'>;
type UpdateEmployeeDto = CreateEmployeeDto;

@Component({
    selector: 'app-employee-form',
    templateUrl: './employee-form.page.html',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        TranslatePipe,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatCardModule,
        MatProgressBarModule
    ]
})
export class EmployeeFormPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly employeeApiService = inject(EmployeeApiService);
    private readonly formBuilder = inject(NonNullableFormBuilder);

    id: number | null = null;

    // Typed, non-nullable reactive form
    readonly employeeForm = this.formBuilder.group({
        firstName: this.formBuilder.control<string>('', [Validators.required]),
        lastName:  this.formBuilder.control<string>('', [Validators.required]),
        role:      this.formBuilder.control<string>('') // omitted Validators.required voluntarily to be able to trigger a backend validation error on submit
    });

    // Page-local state
    readonly loading = signal(false);
    readonly error = signal<string | null>(null);

    async ngOnInit() {
        this.id = parseId(this.route.snapshot.paramMap.get('id'));

        if (this.id != null) { // edit existing employee
            this.loading.set(true);
            this.employeeForm.disable({ emitEvent: false });

            try {
                const entity = await firstValueFrom(this.employeeApiService.getById(this.id));
                this.employeeForm.reset( // reset keeps validators and sets pristine
                    {
                        firstName: entity.firstName ?? '',
                        lastName:  entity.lastName  ?? '',
                        role:      entity.role      ?? ''
                    },
                    { emitEvent: false }
                );
            } catch (err: unknown) {
                const problem = getProblemDetailFromError(err);
                const detail = problem?.detail ?? 'An unexpected error occurred';
                this.error.set(`Failed to load employee with id ${this.id}: ${detail}`);
            } finally {
                this.employeeForm.enable({ emitEvent: false });
                this.loading.set(false);
            }
        }
    }

    async submit() {
        if (this.employeeForm.invalid) {
            this.employeeForm.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        this.error.set(null);
        this.employeeForm.disable({ emitEvent: false });

        const payload: CreateEmployeeDto | UpdateEmployeeDto = {
            firstName: this.employeeForm.controls.firstName.value,
            lastName:  this.employeeForm.controls.lastName.value,
            role:      this.employeeForm.controls.role.value
        };

        try {
            if (this.id == null) {
                await firstValueFrom(this.employeeApiService.create(payload));
            } else {
                await firstValueFrom(this.employeeApiService.update(this.id, payload));
            }
            // Navigate back to list after success
            this.router.navigate(['/employees']);
        } catch (err: any) {
            this.error.set(err?.message ?? 'Save failed');
        } finally {
            this.employeeForm.enable({ emitEvent: false });
            this.loading.set(false);
        }
    }
}
