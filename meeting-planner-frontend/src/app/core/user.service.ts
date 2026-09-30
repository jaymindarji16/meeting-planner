import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from './auth.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<User[]>('/api/users');
  }

  uploadAvatar(file: File) {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<User>('/api/users/me/avatar', form);
  }
}