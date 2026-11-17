import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionService } from './session';

/** Ajoute le jeton aux appels de l'API ; un 401 ferme la session et renvoie a la connexion. */
export const intercepteurJeton: HttpInterceptorFn = (requete, suivant) => {
  const session = inject(SessionService);
  const router = inject(Router);
  const jeton = session.jeton();
  const versApi = requete.url.startsWith('/api/');
  const envoi =
    versApi && jeton ? requete.clone({ setHeaders: { Authorization: `Bearer ${jeton}` } }) : requete;
  return suivant(envoi).pipe(
    catchError((erreur: unknown) => {
      if (erreur instanceof HttpErrorResponse && erreur.status === 401 && !requete.url.endsWith('/connexion')) {
        session.fermer();
        void router.navigate(['/connexion']);
      }
      return throwError(() => erreur);
    }),
  );
};
