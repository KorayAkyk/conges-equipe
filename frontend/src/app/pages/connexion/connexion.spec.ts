import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { SessionService } from '../../core/session';
import { Connexion, MOT_DE_PASSE_DEMO } from './connexion';

describe('Connexion', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [Connexion],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('remplit le formulaire avec un compte de démonstration puis connecte un manager', async () => {
    const fixture = TestBed.createComponent(Connexion);
    const navigation = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    await fixture.whenStable();
    const page = fixture.nativeElement as HTMLElement;

    const boutons = page.querySelectorAll<HTMLButtonElement>('button.lien');
    expect(boutons).toHaveLength(3);
    boutons[1].click();
    page.querySelector('form')?.dispatchEvent(new Event('submit'));

    const requete = TestBed.inject(HttpTestingController).expectOne('/api/auth/connexion');
    expect(requete.request.body).toEqual({ email: 'julie.moreau@brindille.test', motDePasse: MOT_DE_PASSE_DEMO });
    requete.flush({
      jeton: 'j',
      expiration: '2999-01-01T00:00:00Z',
      utilisateur: { id: 2, email: 'julie.moreau@brindille.test', nomComplet: 'Julie', role: 'MANAGER', equipeId: 1, equipeNom: 'Dev' },
    });

    expect(TestBed.inject(SessionService).peutDecider()).toBe(true);
    expect(navigation).toHaveBeenCalledWith(['/validation']);
  });

  it('affiche le message de l API quand la connexion est refusée', async () => {
    const fixture = TestBed.createComponent(Connexion);
    await fixture.whenStable();
    const page = fixture.nativeElement as HTMLElement;
    page.querySelector<HTMLButtonElement>('button.lien')?.click();
    page.querySelector('form')?.dispatchEvent(new Event('submit'));

    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/connexion')
      .flush({ detail: 'E-mail ou mot de passe incorrect.' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(page.querySelector('[role="alert"]')?.textContent).toContain('E-mail ou mot de passe incorrect.');
  });

  it('ne soumet pas un formulaire vide', async () => {
    const fixture = TestBed.createComponent(Connexion);
    await fixture.whenStable();
    (fixture.nativeElement as HTMLElement).querySelector('form')?.dispatchEvent(new Event('submit'));
    TestBed.inject(HttpTestingController).expectNone('/api/auth/connexion');
  });
});
