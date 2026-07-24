import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role-guard';
import { UserRole } from './shared/types/user.type';

import { MainLayout } from './layout/main-layout/main-layout.component';
import { AuthLayout } from './layout/auth-layout/auth-layout.component';

import { LoginPage } from './features/auth/login/login.page';
import { HomePage } from './features/home/home.page';
import { UnauthorizedPage } from './features/error/unauthorized/unauthorized.page';

import { AdminHomePage } from './features/admin/home/home.page';
import { ListUserPage } from './features/admin/user/list-user/list-user.page';
import { CreateUserPage } from './features/admin/user/create-user/create-user.page';
import { EditUser } from './features/admin/user/edit-user/edit-user.page';
import { EditStudent } from './features/admin/student/edit-student/edit-student.page';
import { EditConductor } from './features/admin/conductor/edit-conductor/edit-conductor.page';

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
            {
                path: 'admin/users',
                pathMatch: 'full',
                component: ListUserPage,
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/users/create',
                pathMatch: 'full',
                component: CreateUserPage,
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/users/:id/edit',
                pathMatch: 'full',
                component: EditUser,
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/students/:id/edit',
                pathMatch: 'full',
                component: EditStudent,
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/conductors/:id/edit',
                pathMatch: 'full',
                component: EditConductor,
                canActivate: [roleGuard([UserRole.Admin])],
            }
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
