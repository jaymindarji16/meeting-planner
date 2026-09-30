import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { MeetingService, Meeting } from '../../core/meeting.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-meeting-detail',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe, RouterLink],
  template: `
    <main class="page">
      <a routerLink="/meetings" class="back">← Back</a>

      <p *ngIf="loading()" class="muted">Loading…</p>
      <p *ngIf="error()" class="error">{{ error() }}</p>

      <ng-container *ngIf="meeting() as m">
        <h1>{{ m.title }}</h1>

        <dl class="details">
          <dt>When</dt>
          <dd>{{ m.scheduledAt | date: 'full' }}</dd>

          <dt *ngIf="m.durationMinutes">Duration</dt>
          <dd *ngIf="m.durationMinutes">{{ m.durationMinutes }} minutes</dd>

          <dt *ngIf="m.location">Location</dt>
          <dd *ngIf="m.location">{{ m.location }}</dd>

          <dt>Organizer</dt>
          <dd>{{ m.organizerName }}</dd>

          <dt *ngIf="m.description">Description</dt>
          <dd *ngIf="m.description">{{ m.description }}</dd>
        </dl>

        <h2>Participants</h2>
        <ul class="participants-list">
          <li *ngFor="let p of m.participants">
            {{ p.name }} <span class="muted">({{ p.email }})</span>
            <span class="badge">{{ p.status }}</span>
          </li>
          <li *ngIf="m.participants.length === 0" class="muted">
            No participants invited.
          </li>
        </ul>

        <button
          *ngIf="m.organizerId === auth.current()?.id"
          class="danger"
          (click)="remove(m.id)"
        >
          Delete meeting
        </button>
      </ng-container>
    </main>
  `,
})
export class MeetingDetailComponent implements OnInit {
  meeting = signal<Meeting | null>(null);
  loading = signal<boolean>(true);
  error = signal<string>('');

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private meetingService: MeetingService,
    public auth: AuthService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error.set('Invalid meeting id');
      this.loading.set(false);
      return;
    }

    this.meetingService.get(id).subscribe({
      next: (m) => {
        this.meeting.set(m);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.error ?? 'Meeting not found');
        this.loading.set(false);
      },
    });
  }

  remove(id: number) {
    if (!confirm('Delete this meeting?')) return;
    this.meetingService.delete(id).subscribe(() => {
      this.router.navigate(['/meetings']);
    });
  }
}