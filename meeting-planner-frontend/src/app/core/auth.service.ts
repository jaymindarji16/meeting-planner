import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

export interface User {
  id: number;
  name: string;
  email: string;
  hasAvatar: boolean;
}

const STORAGE_KEY = 'mp.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  /** Currently logged-in user (null when logged out). */
  readonly current = signal<User | null>(this.readFromStorage());

  /** Convenience: is the user logged in? */
  readonly isLoggedIn = computed(() => this.current() !== null);

  constructor(private http: HttpClient, private router: Router) {}

  signup(name: string, email: string, password: string) {
    return this.http
      .post<User>('/api/auth/signup', { name, email, password })
      .pipe(tap((u) => this.setUser(u)));
  }

  login(email: string, password: string) {
    return this.http
      .post<User>('/api/auth/login', { email, password })
      .pipe(tap((u) => this.setUser(u)));
  }

  logout() {
    localStorage.removeItem(STORAGE_KEY);
    this.current.set(null);
    this.router.navigate(['/login']);
  }

  /** Used by the profile page after avatar upload to refresh the session. */
  refresh(user: User) {
    this.setUser(user);
  }

  private setUser(u: User) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(u));
    this.current.set(u);
    this.router.navigate(['/meetings']);
  }

  private readFromStorage(): User | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }
}