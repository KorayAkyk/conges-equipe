import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Demande, Profil } from '../../core/modeles';
import { MesDemandes } from './mes-demandes';

const profil: Profil = {
  utilisateur: { id: 1, email: 'a@exemple.test', nomComplet: 'Alice', role: 'EMPLOYE', equipeId: 1, equipeNom: 'Dev' },
  soldes: [
    { type: 'CONGES_PAYES', libelle: 'Congés payés', acquis: 25, pris: 5, enAttente: 2.5, disponible: 17.5 },
    { type: 'RTT', libelle: 'RTT', acquis: 10, pris: 0, enAttente: 0, disponible: 10 },
  ],
};

const base: Demande = {
  id: 1,
  demandeurId: 1,
  demandeurNom: 'Alice',
  type: 'CONGES_PAYES',
  dateDebut: '2999-12-07',
  dateFin: '2999-12-09',
  debutApresMidi: false,
  finMatin: true,
  jours: 2.5,
  statut: 'EN_ATTENTE',
  motifRefus: null,
  commentaire: null,
  creeLe: '2026-11-16T08:00:00Z',
  joursSousEffectif: [],
};

describe('MesDemandes', () => {
  let serveur: HttpTestingController;

  async function afficher(demandes: Demande[]) {
    TestBed.configureTestingModule({
      imports: [MesDemandes],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    serveur = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(MesDemandes);
    await fixture.whenStable();
    serveur.expectOne('/api/moi').flush(profil);
    serveur.expectOne('/api/demandes/miennes').flush(demandes);
    await fixture.whenStable();
    return { page: fixture.nativeElement as HTMLElement, stable: () => fixture.whenStable() };
  }

  afterEach(() => serveur.verify());

  it('affiche les soldes disponibles', async () => {
    const { page } = await afficher([]);
    expect(page.querySelector('[data-testid="solde-CONGES_PAYES"] .disponible')?.textContent).toContain('17,5 j');
    expect(page.textContent).toContain('Aucune demande pour le moment.');
  });

  it('propose l annulation seulement pour une demande vivante et future', async () => {
    const passee = { ...base, id: 2, dateDebut: '2020-01-06', dateFin: '2020-01-06', statut: 'VALIDEE' as const };
    const refusee = { ...base, id: 3, statut: 'REFUSEE' as const, motifRefus: 'Charge de travail' };
    const { page } = await afficher([base, passee, refusee]);

    const lignes = page.querySelectorAll('[data-testid="ligne-demande"]');
    expect(lignes).toHaveLength(3);
    expect(lignes[0].textContent).toContain('Annuler');
    expect(lignes[1].textContent).not.toContain('Annuler');
    expect(lignes[2].textContent).not.toContain('Annuler');
    expect(lignes[2].textContent).toContain('Motif du refus : Charge de travail');
  });

  it('envoie une demande et affiche le message de l API en cas de refus', async () => {
    const { page, stable } = await afficher([]);
    const saisir = (selecteur: string, valeur: string) => {
      const champ = page.querySelector(selecteur) as HTMLInputElement;
      champ.value = valeur;
      champ.dispatchEvent(new Event('input'));
    };
    saisir('#debut', '2026-12-07');
    saisir('#fin', '2026-12-11');
    page.querySelector('form')?.dispatchEvent(new Event('submit'));

    const requete = serveur.expectOne('/api/demandes');
    expect(requete.request.body).toMatchObject({ type: 'CONGES_PAYES', dateDebut: '2026-12-07', dateFin: '2026-12-11' });
    requete.flush(
      { detail: 'Solde Congés payés insuffisant.', code: 'SOLDE_INSUFFISANT' },
      { status: 422, statusText: 'Unprocessable Entity' },
    );
    await stable();

    expect(page.querySelector('[role="alert"]')?.textContent).toContain('Solde Congés payés insuffisant.');
  });
});
