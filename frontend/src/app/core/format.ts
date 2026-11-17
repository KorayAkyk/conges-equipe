import { HttpErrorResponse } from '@angular/common/http';
import { Statut, TypeConge } from './modeles';

const STATUTS: Record<Statut, string> = {
  EN_ATTENTE: 'En attente',
  VALIDEE: 'Validée',
  REFUSEE: 'Refusée',
  ANNULEE: 'Annulée',
};

const TYPES: Record<TypeConge, string> = {
  CONGES_PAYES: 'Congés payés',
  RTT: 'RTT',
};

export const libelleStatut = (statut: Statut): string => STATUTS[statut];
export const libelleType = (type: TypeConge): string => TYPES[type];

/** "2026-12-08" -> "mar. 8 déc. 2026". La date est lue sans fuseau pour ne jamais glisser d'un jour. */
export function dateLisible(iso: string): string {
  const [annee, mois, jour] = iso.split('-').map(Number);
  return new Intl.DateTimeFormat('fr-FR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(Date.UTC(annee, mois - 1, jour)));
}

/** 3.5 -> "3,5 j" */
export function jours(valeur: number): string {
  return `${valeur.toLocaleString('fr-FR', { maximumFractionDigits: 1 })} j`;
}

/** Date du jour au format AAAA-MM-JJ, dans le fuseau du navigateur. */
export function aujourdhui(maintenant = new Date()): string {
  const m = String(maintenant.getMonth() + 1).padStart(2, '0');
  const j = String(maintenant.getDate()).padStart(2, '0');
  return `${maintenant.getFullYear()}-${m}-${j}`;
}

/** Message a afficher pour une erreur d'API au format Problem Details. */
export function messageErreur(erreur: unknown): string {
  if (erreur instanceof HttpErrorResponse) {
    const corps = erreur.error as { detail?: string } | null;
    if (corps?.detail) {
      return corps.detail;
    }
    if (erreur.status === 0) {
      return 'Le serveur ne répond pas.';
    }
  }
  return 'Une erreur inattendue est survenue.';
}
