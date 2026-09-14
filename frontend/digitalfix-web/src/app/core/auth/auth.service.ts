import { Inject, Injectable } from '@angular/core';
import { MSAL_INSTANCE, MsalBroadcastService, MsalService } from '@azure/msal-angular';
import {
  AccountInfo,
  InteractionStatus,
  IPublicClientApplication
} from '@azure/msal-browser';
import { Subject, filter } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly displayNameSubject = new Subject<string>();
  readonly displayName$ = this.displayNameSubject.asObservable();

  constructor(
    @Inject(MSAL_INSTANCE) private readonly msalInstance: IPublicClientApplication,
    private readonly msalService: MsalService,
    private readonly msalBroadcastService: MsalBroadcastService
  ) {
    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        filter(() => this.isAuthenticated())
      )
      .subscribe(() => this.emitAccountInfo());
  }

  login(): void {
    this.msalService.loginPopup({ scopes: environment.msal.scopes }).subscribe({
      error: (err) => console.error('Error en login', err)
    });
  }

  logout(): void {
    this.msalService.logoutRedirect({
      postLogoutRedirectUri: environment.msal.postLogoutRedirectUri
    });
  }

  isAuthenticated(): boolean {
    return this.msalInstance.getAllAccounts().length > 0;
  }

  getActiveAccount(): AccountInfo | null {
    const accounts = this.msalInstance.getAllAccounts();
    return accounts.length > 0 ? accounts[0] : null;
  }

  getRoles(): string[] {
    const account = this.getActiveAccount();
    const roles = account?.idTokenClaims?.['roles'];
    return Array.isArray(roles) ? (roles as string[]) : [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().some((r) => r.toLowerCase() === role.toLowerCase());
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  getScopes(): string[] {
    const account = this.getActiveAccount();
    const scp = account?.idTokenClaims?.['scp'];
    if (typeof scp === 'string') {
      return scp.split(' ');
    }
    return Array.isArray(scp) ? (scp as string[]) : [];
  }

  private emitAccountInfo(): void {
    const account = this.getActiveAccount();
    this.displayNameSubject.next(account?.name ?? '');
  }
}
