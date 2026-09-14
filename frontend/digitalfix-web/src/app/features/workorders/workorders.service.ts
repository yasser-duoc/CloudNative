import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WorkOrder } from './workorders.model';

@Injectable({ providedIn: 'root' })
export class WorkordersService {
  private readonly baseUrl = `${environment.api.baseUrl}/api/workorders`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<WorkOrder[]> {
    return this.http.get<WorkOrder[]>(this.baseUrl);
  }

  create(order: Partial<WorkOrder>): Observable<WorkOrder> {
    return this.http.post<WorkOrder>(this.baseUrl, order);
  }
}
