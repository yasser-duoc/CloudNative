import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, NgIf],
  template: `
    <nav>
      <a routerLink="/workorders">Órdenes de trabajo</a>
      <span *ngIf="auth.isAuthenticated()">Bienvenido, {{ auth.getActiveAccount()?.name }}</span>
      <button *ngIf="!auth.isAuthenticated()" (click)="auth.login()">Iniciar sesión</button>
      <button *ngIf="auth.isAuthenticated()" (click)="auth.logout()">Cerrar sesión</button>
    </nav>
    <router-outlet></router-outlet>
  `
})
export class AppComponent {
  constructor(public readonly auth: AuthService) {}
}
