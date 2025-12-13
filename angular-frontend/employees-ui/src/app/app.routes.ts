import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';

export const routes: Routes = [
    {
        path: '', // When the user visits the root URL (/), automatically navigate to /home
        redirectTo: 'home',
        pathMatch: 'full'
    },
    {
        path: 'login',
        canActivate: [authGuard],
        loadComponent: () =>
            import('@features/login/login.page').then(m => m.LoginPage),
        title: 'login.title'
    },
    {
        path: '',
        canActivateChild: [authGuard],
        children: [
            {
                path: 'home',
                loadComponent: () =>
                    import('@features/home/home.page').then(m => m.HomePage),
                title: 'home.title'
            },
            {
                path: 'employees',
                loadChildren: () =>
                    import('@features/employee/employee.routes').then(m => m.employeeRoutes),
                title: 'employee.title'
            }
        ]
    },

    // Always last
    {
        path: '**', loadComponent: () =>
            import('@shared/pages/404/not-found.page').then(m => m.NotFoundPage),
        title: 'notFound.title'
    },
];
