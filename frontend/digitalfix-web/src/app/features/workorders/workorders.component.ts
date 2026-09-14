import { Component, OnInit } from '@angular/core';
import { NgFor } from '@angular/common';
import { WorkordersService } from './workorders.service';
import { WorkOrder } from './workorders.model';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-workorders',
  standalone: true,
  imports: [NgFor],
  template: `
    <main style="padding: 1rem">
      <h2>Órdenes de trabajo</h2>
      <p>Roles: {{ auth.getRoles().join(', ') || 'sin roles' }}</p>
      <p>Scopes: {{ auth.getScopes().join(', ') || 'sin scopes' }}</p>
      <button (click)="create()">Crear orden de ejemplo</button>
      <ul>
        <li *ngFor="let order of workOrders">
          #{{ order.id }} - {{ order.customerName }} [{{ order.status }}]
        </li>
      </ul>
    </main>
  `
})
export class WorkordersComponent implements OnInit {
  workOrders: WorkOrder[] = [];

  constructor(
    private readonly workordersService: WorkordersService,
    public readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    this.workordersService.getAll().subscribe((orders) => (this.workOrders = orders));
  }

  create(): void {
    const payload = {
      customerName: 'Cliente Demo',
      serviceId: 1,
      description: 'Mantención eléctrica preventiva'
    };
    this.workordersService.create(payload).subscribe((order) => this.workOrders.push(order));
  }
}
