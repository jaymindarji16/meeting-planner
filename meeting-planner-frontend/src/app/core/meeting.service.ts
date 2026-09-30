import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface ParticipantView {
  id: number;
  name: string;
  email: string;
  hasAvatar: boolean;
  status: string;
}

export interface Meeting {
  id: number;
  title: string;
  description?: string;
  scheduledAt: string; // ISO
  durationMinutes?: number;
  location?: string;
  organizerId: number;
  organizerName: string;
  participants: ParticipantView[];
}

export interface CreateMeeting {
  title: string;
  description?: string;
  scheduledAt: string;
  durationMinutes?: number;
  location?: string;
  participantIds: number[];
}

@Injectable({ providedIn: 'root' })
export class MeetingService {
  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Meeting[]>('/api/meetings');
  }

  get(id: number) {
    return this.http.get<Meeting>(`/api/meetings/${id}`);
  }

  create(body: CreateMeeting) {
    return this.http.post<Meeting>('/api/meetings', body);
  }

  delete(id: number) {
    return this.http.delete<void>(`/api/meetings/${id}`);
  }
}