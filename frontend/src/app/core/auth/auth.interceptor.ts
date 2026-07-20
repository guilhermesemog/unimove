import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  if (req.url.includes('/auth')) {
    return next(req);
  }

  const token = localStorage.getItem('accessToken');

  const authReq = token
    ? req.clone({
      setHeaders: { Authorization: token ? `Bearer ${token}` : '', },
    })
    : req;

  return next(authReq);
};
