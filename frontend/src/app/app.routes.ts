import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role-guard';
import { UserRole } from './shared/types/user.type';

import { MainLayout } from './layout/main-layout/main-layout.component';
import { AuthLayout } from './layout/auth-layout/auth-layout.component';

import { LoginPage } from './features/auth/login/login.page';
import { UnauthorizedPage } from './features/error/unauthorized/unauthorized.page';

export const routes: Routes = [
    {
        path: '',
        component: MainLayout,
        children: [
            {
                path: '',
                pathMatch: 'full',
                loadComponent: () => import('./features/home/home.page').then((module) => module.HomePage),
            },
            {
                path: 'unauthorized',
                pathMatch: 'full',
                component: UnauthorizedPage
            },
            {
                path: 'admin',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/home/home.page').then((module) => module.AdminHomePage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/users',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/user/list-user/list-user.page').then((module) => module.ListUserPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/users/create',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/user/create-user/create-user.page').then((module) => module.CreateUserPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/users/:id/edit',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/user/edit-user/edit-user.page').then((module) => module.EditUserPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/students/:id/edit',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/student/edit-student/edit-student.page').then((module) => module.EditStudentPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/conductors/:id/edit',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/conductor/edit-conductor/edit-conductor.page').then((module) => module.EditConductorPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/universities',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/university/list-university/list-university.page').then((module) => module.ListUniversityPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/universities/create',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/university/create-university/create-university.page').then((module) => module.CreateUniversityPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/universities/:id/edit',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/university/edit-university/edit-university.page').then((module) => module.EditUniversityPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/boarding-stops',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/boarding-stop/list-boarding-stop/list-boarding-stop.page').then((module) => module.ListBoardingStopPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/boarding-stops/create',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/boarding-stop/create-boarding-stop/create-boarding-stop.page').then((module) => module.CreateBoardingStop),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/vehicles',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/vehicle/list-vehicle/list-vehicle.page').then((module) => module.ListVehiclePage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/vehicles/:id/edit',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/vehicle/edit-vehicle/edit-vehicle.page').then((module) => module.EditVehiclePage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/vehicles/create',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/vehicle/create-vehicle/create-vehicle.page').then((module) => module.CreateVehiclePage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/trips',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/trip/list-trip/list-trip.page').then((module) => module.ListTripPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/trips/:id/edit',
                pathMatch: 'full',
                redirectTo: 'admin/trips/:id/view',
            },
            {
                path: 'admin/trips/:id/view',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/trip/view-trip/view-trip.page').then((module) => module.ViewTripPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/interest-lists',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/interest-list/list-interest-list/list-interest-list.page').then((module) => module.ListInterestListPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/interest-lists/create',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/interest-list/create-interest-list/create-interest-list.page').then((module) => module.CreateInterestListPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/interest-lists/:id/edit',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/interest-list/edit-interest-list/edit-interest-list.page').then((module) => module.EditInterestListPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },
            {
                path: 'admin/interest-lists/:id/view',
                pathMatch: 'full',
                loadComponent: () => import('./features/admin/interest-list/view-interest-list/view-interest-list.page').then((module) => module.ViewInterestListPage),
                canActivate: [roleGuard([UserRole.Admin])],
            },

            // STUDENT PAGES

            {
                path: 'bookings',
                pathMatch: 'full',
                loadComponent: () => import('./features/bookings/bookings.page').then((module) => module.BookingsPage),
                canActivate: [roleGuard([UserRole.Student])],
            }, {
                path: 'available-trips',
                pathMatch: 'full',
                loadComponent: () => import('./features/interest-list/interest-list.page').then((module) => module.InterestListPage),
                canActivate: [roleGuard([UserRole.Student])],
            },
            {
                path: 'available-trips/:id',
                pathMatch: 'full',
                loadComponent: () => import('./features/interest-list/trip-checkout/trip-checkout.page').then((module) => module.TripCheckoutPage),
                canActivate: [roleGuard([UserRole.Student])],
            },
            {
                path: 'interest-lists',
                pathMatch: 'full',
                redirectTo: 'available-trips',
            },

            // CONDUCTOR PAGES

            {
                path: 'trips',
                pathMatch: 'full',
                loadComponent: () => import('./features/trips/trips.page').then((module) => module.TripsPage),
                canActivate: [roleGuard([UserRole.Conductor])],
            },
            {
                path: 'trips/:id',
                pathMatch: 'full',
                loadComponent: () => import('./features/trips/trips-detail/trips-detail.page').then((module) => module.TripsDetailPage),
                canActivate: [roleGuard([UserRole.Conductor])],
            },

            // COMMON PAGES

            {
                path: 'user',
                pathMatch: 'full',
                loadComponent: () => import('./features/user/user.page').then((module) => module.UserPage),
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
