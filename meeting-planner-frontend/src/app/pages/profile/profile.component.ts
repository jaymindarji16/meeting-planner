import { Component, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { UserService } from '../../core/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [NgIf, RouterLink],
  template: `
    <main class="page">
      <a routerLink="/meetings" class="back">← Back to meetings</a>
      <h1>Profile</h1>

      <section class="profile">
        <div class="avatar">
          <img
            *ngIf="auth.current()?.hasAvatar; else noAvatar"
            [src]="'/api/users/' + auth.current()!.id + '/avatar?t=' + cacheBust()"
            alt="avatar"
          />
          <ng-template #noAvatar>
            <div class="avatar-placeholder">{{ initial() }}</div>
          </ng-template>
        </div>

        <div>
          <p><strong>{{ auth.current()?.name }}</strong></p>
          <p class="muted">{{ auth.current()?.email }}</p>
        </div>
      </section>

      <section>
        <label class="upload">
          Upload avatar (PNG or JPG, max 2 MB)
          <input
            type="file"
            accept="image/png,image/jpeg"
            (change)="onFile($event)"
          />
        </label>
        <p class="muted" *ngIf="message()">{{ message() }}</p>
      </section>
    </main>
  `,
})
export class ProfileComponent {
  message = signal<string>('');
  cacheBust = signal<number>(Date.now());

  constructor(public auth: AuthService, private userService: UserService) {}

  initial(): string {
    return (this.auth.current()?.name?.[0] ?? '?').toUpperCase();
  }

  onFile(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.message.set('Uploading…');
    this.userService.uploadAvatar(file).subscribe({
      next: (u) => {
        this.auth.refresh(u);
        this.cacheBust.set(Date.now());
        this.message.set('Avatar updated.');
        input.value = ''; // allow re-uploading the same file later
      },
      error: (err) => {
        this.message.set(err?.error?.error ?? 'Upload failed');
      },
    });
  }
}