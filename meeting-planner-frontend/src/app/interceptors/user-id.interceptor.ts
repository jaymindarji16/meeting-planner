import { HttpInterceptorFn } from '@angular/common/http';

export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const raw = localStorage.getItem('mp.user');
  if (raw) {
    try {
      const user = JSON.parse(raw);
      req = req.clone({
        setHeaders: { 'X-User-Id': String(user.id) },
      });
    } catch {
      // ignore malformed storage
    }
  }
  return next(req);
};