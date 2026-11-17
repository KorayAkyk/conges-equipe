import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session';

export const connecte: CanActivateFn = () =>
  inject(SessionService).estConnecte() || inject(Router).createUrlTree(['/connexion']);

export const decideur: CanActivateFn = () =>
  inject(SessionService).peutDecider() || inject(Router).createUrlTree(['/mes-demandes']);
