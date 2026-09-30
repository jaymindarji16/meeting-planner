import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-card">
      <h1>Welcome back</h1>
      <p class="muted">Log in to see your meetings.</p>

      <label>
        Email
        <input [(ngModel)]="email" type="email" placeholder="you@example.com" />
      </label>

      <label>
        Password
        <input [(ngModel)]="password" type="password" placeholder="••••••" />
      </label>

      <button (click)="submit()" [disabled]="loading()">
        {{ loading() ? 'Logging in…' : 'Log in' }}
      </button>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <p class="muted">
        No account? <a routerLink="/signup">Sign up</a>
      </p>
    </div>
  `,
})
export class LoginComponent {
  email = '';
  password = '';
  error = signal<string>('');
  loading = signal<boolean>(false);

  constructor(private auth: AuthService) {}

  submit() {
    this.error.set('');
    this.loading.set(true);

    this.auth.login(this.email, this.password).subscribe({
      next: () => this.loading.set(false),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.error ?? 'Login failed');
      },
    });
  }
}