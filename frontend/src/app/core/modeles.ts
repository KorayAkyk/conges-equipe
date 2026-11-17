// Types des reponses de l'API (voir le document OpenAPI servi par le backend).

export type Role = 'EMPLOYE' | 'MANAGER' | 'RH';
export type TypeConge = 'CONGES_PAYES' | 'RTT';
export type Statut = 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE' | 'ANNULEE';

export interface Utilisateur {
  id: number;
  email: string;
  nomComplet: string;
  role: Role;
  equipeId: number | null;
  equipeNom: string | null;
}

export interface ReponseConnexion {
  jeton: string;
  expiration: string;
  utilisateur: Utilisateur;
}

export interface Solde {
  type: TypeConge;
  libelle: string;
  acquis: number;
  pris: number;
  enAttente: number;
  disponible: number;
}

export interface Profil {
  utilisateur: Utilisateur;
  soldes: Solde[];
}

export interface Demande {
  id: number;
  demandeurId: number;
  demandeurNom: string;
  type: TypeConge;
  dateDebut: string;
  dateFin: string;
  debutApresMidi: boolean;
  finMatin: boolean;
  jours: number;
  statut: Statut;
  motifRefus: string | null;
  commentaire: string | null;
  creeLe: string;
  joursSousEffectif: string[];
}

export interface NouvelleDemande {
  type: TypeConge;
  dateDebut: string;
  dateFin: string;
  debutApresMidi: boolean;
  finMatin: boolean;
  commentaire: string | null;
}

export interface Evenement {
  ancienStatut: Statut | null;
  nouveauStatut: Statut;
  auteurNom: string;
  commentaire: string | null;
  horodatage: string;
}

export interface Equipe {
  id: number;
  nom: string;
}

export interface CalendrierEquipe {
  equipeId: number;
  equipeNom: string;
  mois: string;
  joursFeries: { date: string; libelle: string }[];
  membres: { id: number; nomComplet: string }[];
  absences: Demande[];
}
