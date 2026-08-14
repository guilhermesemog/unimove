import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { UserRole } from '../../shared/types/user.type';

export const roleGuard = (allowedRoles: UserRole[]): CanActivateFn => {
  return async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    try {
      const user = await firstValueFrom(authService.identify());

      if (!allowedRoles.includes(user.role)) {
        await router.navigate(['/unauthorized']);
        return false;
      }

      return true;
    } catch {
      await router.navigate(['/unauthorized']);
      return false;
    }
  };
};