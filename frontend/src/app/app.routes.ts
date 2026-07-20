import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role-guard';
import { UserRole } from './shared/types/user.type';

import { LoginPage } from './features/auth/login/login.page';
import { HomePage } from './features/home/home.page';
import { AdminHomePage } from './features/admin/home/home.page';
import { UnauthorizedPage } from './features/error/unauthorized/unauthorized.page';

export const routes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        component: HomePage
    },
    {
        path: 'login',
        pathMatch: 'full',
        component: LoginPage
    },
    {
        path: 'admin',
        pathMatch: 'full',
        component: AdminHomePage,
        canActivate: [roleGuard([UserRole.Admin])],
    },
    {
        path: 'unauthorized',
        pathMatch: 'full',
        component: UnauthorizedPage
    }
];
