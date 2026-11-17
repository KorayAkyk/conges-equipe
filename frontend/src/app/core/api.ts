import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CalendrierEquipe,
  Demande,
  Equipe,
  Evenement,
  NouvelleDemande,
  Profil,
  ReponseConnexion,
} from './modeles';

/** Appels a l'API REST. Les URL sont relatives : le serveur de dev ou nginx relaie /api. */
@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);

  connexion(email: string, motDePasse: string): Observable<ReponseConnexion> {
    return this.http.post<ReponseConnexion>('/api/auth/connexion', { email, motDePasse });
  }

  profil(): Observable<Profil> {
    return this.http.get<Profil>('/api/moi');
  }

  mesDemandes(): Observable<Demande[]> {
    return this.http.get<Demande[]>('/api/demandes/miennes');
  }

  creer(demande: NouvelleDemande): Observable<Demande> {
    return this.http.post<Demande>('/api/demandes', demande);
  }

  annuler(id: number): Observable<Demande> {
    return this.http.post<Demande>(`/api/demandes/${id}/annuler`, {});
  }

  aDecider(): Observable<Demande[]> {
    return this.http.get<Demande[]>('/api/demandes/a-decider');
  }

  valider(id: number): Observable<Demande> {
    return this.http.post<Demande>(`/api/demandes/${id}/valider`, {});
  }

  refuser(id: number, motif: string): Observable<Demande> {
    return this.http.post<Demande>(`/api/demandes/${id}/refuser`, { motif });
  }

  historique(id: number): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`/api/demandes/${id}/historique`);
  }

  equipes(): Observable<Equipe[]> {
    return this.http.get<Equipe[]>('/api/equipes');
  }

  calendrier(mois: string, equipeId?: number): Observable<CalendrierEquipe> {
    const params: Record<string, string> = { mois };
    if (equipeId) {
      params['equipeId'] = String(equipeId);
    }
    return this.http.get<CalendrierEquipe>('/api/calendrier', { params });
  }
}
