import { Injectable, computed, signal } from '@angular/core';
import { ReponseConnexion, Utilisateur } from './modeles';

const CLE = 'conges.session';

interface Session {
  jeton: string;
  expiration: string;
  utilisateur: Utilisateur;
}

/**
 * Session de l'utilisateur connecte. Le jeton est garde dans sessionStorage : il disparait a la
 * fermeture de l'onglet et n'est jamais envoye automatiquement comme le serait un cookie.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly session = signal<Session | null>(lire());

  readonly utilisateur = computed(() => this.session()?.utilisateur ?? null);
  readonly jeton = computed(() => this.session()?.jeton ?? null);
  readonly estConnecte = computed(() => this.session() !== null);
  readonly peutDecider = computed(() => {
    const role = this.utilisateur()?.role;
    return role === 'MANAGER' || role === 'RH';
  });

  ouvrir(reponse: ReponseConnexion): void {
    const session: Session = {
      jeton: reponse.jeton,
      expiration: reponse.expiration,
      utilisateur: reponse.utilisateur,
    };
    sessionStorage.setItem(CLE, JSON.stringify(session));
    this.session.set(session);
  }

  fermer(): void {
    sessionStorage.removeItem(CLE);
    this.session.set(null);
  }
}

function lire(): Session | null {
  try {
    const brut = sessionStorage.getItem(CLE);
    if (!brut) {
      return null;
    }
    const session = JSON.parse(brut) as Session;
    return new Date(session.expiration).getTime() > Date.now() ? session : null;
  } catch {
    return null;
  }
}
