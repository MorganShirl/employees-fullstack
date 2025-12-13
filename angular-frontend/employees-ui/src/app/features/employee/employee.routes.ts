import { Routes } from '@angular/router';
import { EmployeeListStore } from '@features/employee/pages/list/employee-list.store';

export const employeeRoutes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/list/employee-list.page').then(m => m.EmployeeListPage),
        providers: [EmployeeListStore] // scoped to this route only
    },
    {
        path: 'new',
        loadComponent: () =>
            import('./pages/form/employee-form.page').then(m => m.EmployeeFormPage),
    },
    {
        path: ':id',
        loadComponent: () =>
            import('./pages/form/employee-form.page').then(m => m.EmployeeFormPage)
    }
];
