import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { SignupComponent } from './pages/signup/signup.component';
import { MeetingListComponent } from './pages/meeting-list/meeting-list.component';
import { MeetingCreateComponent } from './pages/meeting-create/meeting-create.component';
import { MeetingDetailComponent } from './pages/meeting-detail/meeting-detail.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { authGuard, guestGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'meetings', pathMatch: 'full' },

  // Public — only reachable when NOT logged in
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'signup', component: SignupComponent, canActivate: [guestGuard] },

  // Protected — require a logged-in user
  { path: 'meetings', component: MeetingListComponent, canActivate: [authGuard] },
  { path: 'meetings/new', component: MeetingCreateComponent, canActivate: [authGuard] },
  { path: 'meetings/:id', component: MeetingDetailComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: 'meetings' },
];