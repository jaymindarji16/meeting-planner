import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MeetingService } from '../../core/meeting.service';
import { UserService } from '../../core/user.service';
import { User } from '../../core/auth.service';

@Component({
  selector: 'app-meeting-create',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, RouterLink],
  template: `
    <main class="page">
      <a routerLink="/meetings" class="back">← Back</a>
      <h1>New meeting</h1>

      <div class="form">
        <label>
          Title
          <input [(ngModel)]="title" type="text" placeholder="Weekly sync" />
        </label>

        <label>
          Description
          <textarea [(ngModel)]="description" rows="3" placeholder="Optional"></textarea>
        </label>

        <label>
          When
          <input [(ngModel)]="scheduledAt" type="datetime-local" />
        </label>

        <label>
          Duration (minutes)
          <input [(ngModel)]="durationMinutes" type="number" min="1" max="1440" />
        </label>

        <label>
          Location
          <input [(ngModel)]="location" type="text" placeholder="Room / link (optional)" />
        </label>

        <fieldset class="participants">
          <legend>Participants</legend>
          <p *ngIf="users().length === 0" class="muted">No other users yet.</p>
          <label *ngFor="let u of users()" class="checkbox-row">
            <input
              type="checkbox"
              [checked]="selected.has(u.id)"
              (change)="toggle(u.id)"
            />
            <span>{{ u.name }} <span class="muted">({{ u.email }})</span></span>
          </label>
        </fieldset>

        <button (click)="submit()" [disabled]="loading()">
          {{ loading() ? 'Creating…' : 'Create meeting' }}
        </button>

        <p class="error" *ngIf="error()">{{ error() }}</p>
      </div>
    </main>
  `,
})
export class MeetingCreateComponent implements OnInit {
  title = '';
  description = '';
  scheduledAt = '';
  durationMinutes?: number;
  location = '';

  users = signal<User[]>([]);
  selected = new Set<number>();
  error = signal<string>('');
  loading = signal<boolean>(false);

  constructor(
    private meetingService: MeetingService,
    private userService: UserService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.userService.list().subscribe((u) => this.users.set(u));
  }

  toggle(id: number) {
    if (this.selected.has(id)) {
      this.selected.delete(id);
    } else {
      this.selected.add(id);
    }
  }

  submit() {
    this.error.set('');
    this.loading.set(true);

    this.meetingService
      .create({
        title: this.title,
        description: this.description || undefined,
        scheduledAt: this.scheduledAt,
        durationMinutes: this.durationMinutes,
        location: this.location || undefined,
        participantIds: Array.from(this.selected),
      })
      .subscribe({
        next: (m) => this.router.navigate(['/meetings', m.id]),
        error: (err) => {
          this.loading.set(false);
          this.error.set(err?.error?.error ?? 'Failed to create meeting');
        },
      });
  }
}