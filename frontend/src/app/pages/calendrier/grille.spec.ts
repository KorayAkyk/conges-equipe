import { CalendrierEquipe, Demande } from '../../core/modeles';
import { construireGrille, joursDuMois, libelleMois, moisVoisin } from './grille';

function absence(partiel: Partial<Demande>): Demande {
  return {
    id: 1,
    demandeurId: 1,
    demandeurNom: 'Alice',
    type: 'CONGES_PAYES',
    dateDebut: '2026-12-04',
    dateFin: '2026-12-08',
    debutApresMidi: false,
    finMatin: false,
    jours: 3,
    statut: 'VALIDEE',
    motifRefus: null,
    commentaire: null,
    creeLe: '2026-11-16T08:00:00Z',
    joursSousEffectif: [],
    ...partiel,
  };
}

describe('grille du calendrier', () => {
  const noel = [{ date: '2026-12-25', libelle: 'Noël' }];

  it('liste les jours du mois avec week-ends et jours fériés', () => {
    const jours = joursDuMois('2026-12', noel);
    expect(jours).toHaveLength(31);
    expect(jours[0]).toMatchObject({ date: '2026-12-01', initiale: 'M', travaille: true });
    expect(jours[4]).toMatchObject({ date: '2026-12-05', initiale: 'S', travaille: false });
    expect(jours[24]).toMatchObject({ date: '2026-12-25', travaille: false, ferie: 'Noël' });
  });

  it('gère février des années bissextiles', () => {
    expect(joursDuMois('2028-02', [])).toHaveLength(29);
    expect(joursDuMois('2027-02', [])).toHaveLength(28);
  });

  it('place les absences sur les jours travaillés seulement', () => {
    const calendrier: CalendrierEquipe = {
      equipeId: 1,
      equipeNom: 'Dev',
      mois: '2026-12',
      joursFeries: noel,
      membres: [
        { id: 1, nomComplet: 'Alice' },
        { id: 2, nomComplet: 'Bruno' },
      ],
      absences: [
        absence({ finMatin: true }),
        absence({ id: 2, demandeurId: 2, statut: 'EN_ATTENTE', dateDebut: '2026-12-10', dateFin: '2026-12-10', debutApresMidi: true }),
      ],
    };
    const { lignes } = construireGrille(calendrier);
    const alice = lignes[0].cellules;
    expect(alice[3]).toEqual({ statut: 'VALIDEE', type: 'CONGES_PAYES', demi: null });
    expect(alice[4].statut).toBeNull();
    expect(alice[5].statut).toBeNull();
    expect(alice[6].statut).toBe('VALIDEE');
    expect(alice[7]).toEqual({ statut: 'VALIDEE', type: 'CONGES_PAYES', demi: 'matin' });
    expect(alice[8].statut).toBeNull();
    expect(lignes[1].cellules[9]).toEqual({ statut: 'EN_ATTENTE', type: 'CONGES_PAYES', demi: 'apres-midi' });
  });

  it('passe d un mois à l autre, y compris en changeant d année', () => {
    expect(moisVoisin('2026-12', 1)).toBe('2027-01');
    expect(moisVoisin('2026-01', -1)).toBe('2025-12');
  });

  it('nomme le mois en français', () => {
    expect(libelleMois('2026-12')).toBe('décembre 2026');
  });
});
