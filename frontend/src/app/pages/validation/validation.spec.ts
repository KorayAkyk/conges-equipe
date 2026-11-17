import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Demande } from '../../core/modeles';
import { Validation } from './validation';

const demande: Demande = {
  id: 7,
  demandeurId: 3,
  demandeurNom: 'Léa Roussel',
  type: 'CONGES_PAYES',
  dateDebut: '2026-12-02',
  dateFin: '2026-12-03',
  debutApresMidi: false,
  finMatin: false,
  jours: 2,
  statut: 'EN_ATTENTE',
  motifRefus: null,
  commentaire: 'Déménagement',
  creeLe: '2026-11-16T08:00:00Z',
  joursSousEffectif: ['2026-12-02', '2026-12-03'],
};

function bouton(page: HTMLElement, texte: string): HTMLButtonElement | undefined {
  return [...page.querySelectorAll('button')].find((b) => b.textContent?.trim() === texte);
}

describe('Validation', () => {
  let serveur: HttpTestingController;

  async function afficher(): Promise<{ page: HTMLElement; stable: () => Promise<unknown> }> {
    TestBed.configureTestingModule({
      imports: [Validation],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    serveur = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(Validation);
    await fixture.whenStable();
    serveur.expectOne('/api/demandes/a-decider').flush([demande]);
    await fixture.whenStable();
    return { page: fixture.nativeElement as HTMLElement, stable: () => fixture.whenStable() };
  }

  afterEach(() => serveur.verify());

  it('affiche la demande et l avertissement de sous-effectif', async () => {
    const { page } = await afficher();
    expect(page.querySelectorAll('[data-testid="demande-a-valider"]')).toHaveLength(1);
    expect(page.textContent).toContain('Léa Roussel');
    expect(page.querySelector('.avertissement')?.textContent).toContain('Plus de la moitié');
  });

  it('valide la demande, signale le sous-effectif et recharge la liste', async () => {
    const { page, stable } = await afficher();
    bouton(page, 'Valider')?.click();

    const requete = serveur.expectOne('/api/demandes/7/valider');
    expect(requete.request.method).toBe('POST');
    requete.flush({ ...demande, statut: 'VALIDEE' });
    serveur.expectOne('/api/demandes/a-decider').flush([]);
    await stable();

    expect(page.querySelector('[role="status"]')?.textContent).toContain('Léa Roussel validée');
    expect(page.querySelector('[role="status"]')?.textContent).toContain('Attention');
    expect(page.textContent).toContain('Aucune demande en attente.');
  });

  it('envoie le motif saisi lors d un refus', async () => {
    const { page, stable } = await afficher();
    bouton(page, 'Refuser')?.click();
    await stable();

    const zone = page.querySelector('textarea') as HTMLTextAreaElement;
    zone.value = 'Clôture comptable';
    zone.dispatchEvent(new Event('input'));
    bouton(page, 'Confirmer le refus')?.click();

    const requete = serveur.expectOne('/api/demandes/7/refuser');
    expect(requete.request.body).toEqual({ motif: 'Clôture comptable' });
    requete.flush({ ...demande, statut: 'REFUSEE', motifRefus: 'Clôture comptable' });
    serveur.expectOne('/api/demandes/a-decider').flush([]);
  });
});
