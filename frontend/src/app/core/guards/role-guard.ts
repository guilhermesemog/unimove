import { CanActivateFn, Router } from '@angular/router';
import { UserRole } from '../../shared/types/user.type';

import { AuthService } from '../auth/auth.service';
import { inject } from '@angular/core/primitives/di';

export const roleGuard = (allowedRoles: UserRole[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const userRole = authService.getRole();

    if (!userRole || !allowedRoles.includes(userRole)) {
      router.navigate(['/unauthorized']);
      return false;
    }

    return true;
  }
};
