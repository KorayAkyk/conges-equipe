import { Routes } from '@angular/router';
import { connecte, decideur } from './core/gardes';

export const routes: Routes = [
  { path: 'connexion', loadComponent: () => import('./pages/connexion/connexion').then((m) => m.Connexion) },
  {
    path: 'mes-demandes',
    canActivate: [connecte],
    loadComponent: () => import('./pages/mes-demandes/mes-demandes').then((m) => m.MesDemandes),
  },
  {
    path: 'validation',
    canActivate: [connecte, decideur],
    loadComponent: () => import('./pages/validation/validation').then((m) => m.Validation),
  },
  {
    path: 'calendrier',
    canActivate: [connecte],
    loadComponent: () => import('./pages/calendrier/calendrier').then((m) => m.Calendrier),
  },
  { path: '', pathMatch: 'full', redirectTo: 'mes-demandes' },
  { path: '**', redirectTo: 'mes-demandes' },
];
