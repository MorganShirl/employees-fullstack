import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Employee } from '@shared/models/employee.model';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class EmployeeApiService {
    private readonly baseUrl = environment.apiBaseUrl;

    constructor(private readonly httpClient: HttpClient) {}

    getAll() {
        return this.httpClient.get<Employee[]>(
            `${this.baseUrl}/employees`,
            { withCredentials: true } // required in CORS mode so browser sends JSESSIONID cookie
        );
    }

    getById(id: number) {
        return this.httpClient.get<Employee>(
            `${this.baseUrl}/employees/${id}`,
            { withCredentials: true });
    }

    create(payload: Omit<Employee, 'id'>) {
        return this.httpClient.post<Employee>(
            `${this.baseUrl}/employees`, payload,
            { withCredentials: true }
        );
    }

    update(id: number, payload: Omit<Employee, 'id'>) {
        return this.httpClient.put<Employee>(
            `${this.baseUrl}/employees/${id}`, payload,
            { withCredentials: true }
        );
    }

    delete(id: number) {
        return this.httpClient.delete<void>(
            `${this.baseUrl}/employees/${id}`,
            { withCredentials: true }
        );
    }
}
