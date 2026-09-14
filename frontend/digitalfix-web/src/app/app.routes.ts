import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { WorkordersComponent } from './features/workorders/workorders.component';
import { RoleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'workorders', pathMatch: 'full' },
  {
    path: 'workorders',
    component: WorkordersComponent,
    canActivate: [MsalGuard, RoleGuard],
    data: { roles: ['Admin', 'Supervisor', 'Cliente'] }
  },
  { path: '**', redirectTo: 'workorders' }
];
