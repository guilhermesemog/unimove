import { CanActivateFn, Router } from '@angular/router';
import { UserRole } from '../../shared/types/user.type';

import { AuthService } from '../auth/auth.service';
import { inject } from '@angular/core/primitives/di';

export const roleGuard = (allowedRoles: UserRole[]): CanActivateFn => {
  return async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const userRole = await authService.identify().then((user) => user.role);

    if (!userRole || !allowedRoles.includes(userRole)) {
      router.navigate(['/unauthorized']);
      return false;
    }

    return true;
  }
};
