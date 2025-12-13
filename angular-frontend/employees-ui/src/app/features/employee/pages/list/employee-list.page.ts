import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmployeeListStore } from '@features/employee/pages/list/employee-list.store';
import { TranslatePipe } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { EmployeeApiService } from '@core/services/employee-api.service';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatCard } from '@angular/material/card';

@Component({
    selector: 'app-employees-list',
    templateUrl: './employee-list.page.html',
    imports: [
        RouterLink,
        TranslatePipe,
        MatTableModule,
        MatSortModule,
        MatButtonModule,
        MatCard
    ]
})
export class EmployeeListPage implements OnInit {
    public readonly employeeListStore = inject(EmployeeListStore);
    private readonly employeeApiService = inject(EmployeeApiService);

    public readonly tableColumnNames = ['id', 'firstName', 'lastName', 'role', 'actions'];

    ngOnInit() { this.employeeListStore.loadAll(); }

    applySort(sort: Sort) {
        const data = [...this.employeeListStore.employees()];

        if (!sort.active || sort.direction === '') {
            return;
        }

        data.sort((rowA, rowB) => {
            const valueA = (rowA as any)[sort.active];
            const valueB = (rowB as any)[sort.active];

            const comparison = valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
            return sort.direction === 'asc' ? comparison : -comparison;
        });

        // Update your signal
        this.employeeListStore.employees.set(data);
    }

    async delete(id: number) {
        await firstValueFrom(this.employeeApiService.delete(id));
        await this.employeeListStore.loadAll();
    }
}
