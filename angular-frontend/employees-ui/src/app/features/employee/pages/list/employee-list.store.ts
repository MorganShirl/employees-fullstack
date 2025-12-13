import { Injectable, signal } from '@angular/core';
import { Employee } from '@shared/models/employee.model';
import { firstValueFrom } from 'rxjs';
import { EmployeeApiService } from '@core/services/employee-api.service';
import { getProblemDetailFromError } from '@shared/utils/error.util';

@Injectable()
export class EmployeeListStore {
    // Signals for list local state
    public readonly employees = signal<Employee[]>([]);
    public readonly loading = signal(false);
    public readonly error = signal<string | null>(null);

    constructor(private readonly employeeApiService: EmployeeApiService) {}

    public async loadAll(): Promise<void> {
        if (this.loading()) return; // prevent overlapping calls

        this.loading.set(true);
        this.error.set(null);
        try {
            const employeeList = await firstValueFrom(this.employeeApiService.getAll());
            this.employees.set(employeeList);
        } catch (err) {
            console.error('[EmployeeListStore.loadAll]', err);
            this.error.set(this.toMessage(err));
        } finally {
            this.loading.set(false);
        }
    }

    private toMessage(err: unknown): string {
        const problem = getProblemDetailFromError(err);
        return problem?.detail ?? 'An unexpected error occurred';
    }
}
