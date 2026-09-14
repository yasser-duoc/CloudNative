import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    const requiredRoles: string[] = route.data['roles'] ?? [];

    if (!this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/']);
    }

    if (requiredRoles.length > 0 && !this.authService.hasAnyRole(requiredRoles)) {
      return this.router.createUrlTree(['/']);
    }

    return true;
  }
}
