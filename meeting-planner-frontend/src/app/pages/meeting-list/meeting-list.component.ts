import { Component, OnInit, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MeetingService, Meeting } from '../../core/meeting.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-meeting-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, RouterLink],
  template: `
    <header class="app-bar">
      <div>
        <strong>Meeting Planner</strong>
      </div>
      <div class="actions">
        <span class="muted">Hi, {{ auth.current()?.name }}</span>
        <a routerLink="/profile" class="link">Profile</a>
        <button class="ghost" (click)="auth.logout()">Log out</button>
      </div>
    </header>

    <main class="page">
      <div class="page-header">
        <h1>My meetings</h1>
        <a routerLink="/meetings/new" class="btn">+ New meeting</a>
      </div>

      <p *ngIf="loading()" class="muted">Loading…</p>
      <p *ngIf="!loading() && meetings().length === 0" class="empty">
        No meetings yet. Create your first one.
      </p>

      <ul class="meeting-list">
        <li *ngFor="let m of meetings()" class="meeting-item">
          <a [routerLink]="['/meetings', m.id]" class="meeting-link">
            <div class="meeting-title">{{ m.title }}</div>
            <div class="meeting-meta">
              {{ m.scheduledAt | date: 'medium' }}
              · organizer: {{ m.organizerName }}
              · {{ m.participants.length }} participant(s)
            </div>
          </a>
        </li>
      </ul>
    </main>
  `,
})
export class MeetingListComponent implements OnInit {
  meetings = signal<Meeting[]>([]);
  loading = signal<boolean>(true);

  constructor(public auth: AuthService, private meetingService: MeetingService) {}

  ngOnInit(): void {
    this.meetingService.list().subscribe({
      next: (list) => {
        this.meetings.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}