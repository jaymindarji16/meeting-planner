import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Blocks navigation to protected routes when there is no logged-in user.
 * Redirects to /login instead.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};

/**
 * Blocks navigation to /login and /signup when a user is already logged in.
 * Redirects to /meetings instead. Prevents confusing "logged-in user sees login page".
 */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return true;
  }
  router.navigate(['/meetings']);
  return false;
};