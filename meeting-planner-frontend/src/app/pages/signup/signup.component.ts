import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-card">
      <h1>Create your account</h1>

      <label>
        Name
        <input [(ngModel)]="name" type="text" placeholder="Your name" />
      </label>

      <label>
        Email
        <input [(ngModel)]="email" type="email" placeholder="you@example.com" />
      </label>

      <label>
        Password
        <input [(ngModel)]="password" type="password" placeholder="At least 6 characters" />
      </label>

      <button (click)="submit()" [disabled]="loading()">
        {{ loading() ? 'Creating…' : 'Create account' }}
      </button>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <p class="muted">
        Already have an account? <a routerLink="/login">Log in</a>
      </p>
    </div>
  `,
})
export class SignupComponent {
  name = '';
  email = '';
  password = '';
  error = signal<string>('');
  loading = signal<boolean>(false);

  constructor(private auth: AuthService) {}

  submit() {
    this.error.set('');
    this.loading.set(true);

    this.auth.signup(this.name, this.email, this.password).subscribe({
      next: () => this.loading.set(false),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.error ?? 'Signup failed');
      },
    });
  }
}