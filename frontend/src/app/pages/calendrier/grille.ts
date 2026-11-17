import { CalendrierEquipe, Statut, TypeConge } from '../../core/modeles';

// Construction de la grille du calendrier d'equipe : une ligne par membre, une colonne par jour.
// Fonctions pures, testees sans navigateur.

export interface JourGrille {
  date: string;
  numero: number;
  initiale: string;
  travaille: boolean;
  ferie: string | null;
}

export interface Cellule {
  statut: Statut | null;
  type: TypeConge | null;
  demi: 'matin' | 'apres-midi' | null;
}

export interface LigneGrille {
  membre: string;
  cellules: Cellule[];
}

const INITIALES = ['D', 'L', 'M', 'M', 'J', 'V', 'S'];
const VIDE: Cellule = { statut: null, type: null, demi: null };

export function joursDuMois(mois: string, feries: { date: string; libelle: string }[]): JourGrille[] {
  const [annee, numeroMois] = mois.split('-').map(Number);
  const nombre = new Date(Date.UTC(annee, numeroMois, 0)).getUTCDate();
  const jours: JourGrille[] = [];
  for (let j = 1; j <= nombre; j++) {
    const d = new Date(Date.UTC(annee, numeroMois - 1, j));
    const date = d.toISOString().slice(0, 10);
    const ferie = feries.find((f) => f.date === date)?.libelle ?? null;
    const jourSemaine = d.getUTCDay();
    jours.push({
      date,
      numero: j,
      initiale: INITIALES[jourSemaine],
      travaille: jourSemaine !== 0 && jourSemaine !== 6 && ferie === null,
      ferie,
    });
  }
  return jours;
}

export function construireGrille(calendrier: CalendrierEquipe): { jours: JourGrille[]; lignes: LigneGrille[] } {
  const jours = joursDuMois(calendrier.mois, calendrier.joursFeries);
  const lignes = calendrier.membres.map((membre) => ({
    membre: membre.nomComplet,
    cellules: jours.map((jour) => {
      if (!jour.travaille) {
        return VIDE;
      }
      const absence = calendrier.absences.find(
        (a) => a.demandeurId === membre.id && a.dateDebut <= jour.date && jour.date <= a.dateFin,
      );
      if (!absence) {
        return VIDE;
      }
      let demi: Cellule['demi'] = null;
      if (absence.debutApresMidi && jour.date === absence.dateDebut) {
        demi = 'apres-midi';
      } else if (absence.finMatin && jour.date === absence.dateFin) {
        demi = 'matin';
      }
      return { statut: absence.statut, type: absence.type, demi };
    }),
  }));
  return { jours, lignes };
}

/** "2026-12" et +1 -> "2027-01" */
export function moisVoisin(mois: string, decalage: number): string {
  const [annee, numeroMois] = mois.split('-').map(Number);
  const d = new Date(Date.UTC(annee, numeroMois - 1 + decalage, 1));
  return d.toISOString().slice(0, 7);
}

/** "2026-12" -> "décembre 2026" */
export function libelleMois(mois: string): string {
  const [annee, numeroMois] = mois.split('-').map(Number);
  return new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric', timeZone: 'UTC' }).format(
    new Date(Date.UTC(annee, numeroMois - 1, 1)),
  );
}
