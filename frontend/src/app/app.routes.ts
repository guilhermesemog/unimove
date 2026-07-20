import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role-guard';
import { UserRole } from './shared/types/user.type';

import { MainLayout } from './layout/main-layout/main-layout.component';
import { AuthLayout } from './layout/auth-layout/auth-layout.component';

import { LoginPage } from './features/auth/login/login.page';
import { HomePage } from './features/home/home.page';
import { AdminHomePage } from './features/admin/home/home.page';
import { UnauthorizedPage } from './features/error/unauthorized/unauthorized.page';

export const routes: Routes = [
    {
        path: '',
        component: MainLayout,
        children: [
            {
                path: '',
                pathMatch: 'full',
                component: HomePage
            },
            {
                path: 'unauthorized',
                pathMatch: 'full',
                component: UnauthorizedPage
            },
            {
                path: 'admin',
                pathMatch: 'full',
                component: AdminHomePage,
                canActivate: [roleGuard([UserRole.Admin])],
            },
        ]
    },
    {
        path: '',
        component: AuthLayout,
        children: [
            {
                path: 'login',
                pathMatch: 'full',
                component: LoginPage
            }
        ]
    }
];
