import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { intercepteurJeton } from './intercepteur-jeton';
import { SessionService } from './session';

describe('intercepteurJeton', () => {
  let http: HttpClient;
  let serveur: HttpTestingController;
  let session: SessionService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([intercepteurJeton])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    serveur = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    session.ouvrir({
      jeton: 'abc',
      expiration: '2999-01-01T00:00:00Z',
      utilisateur: { id: 1, email: 'a@exemple.test', nomComplet: 'Alice', role: 'EMPLOYE', equipeId: 1, equipeNom: 'Dev' },
    });
  });

  afterEach(() => serveur.verify());

  it('ajoute le jeton aux appels de l API', () => {
    http.get('/api/moi').subscribe();
    expect(serveur.expectOne('/api/moi').request.headers.get('Authorization')).toBe('Bearer abc');
  });

  it('n envoie pas le jeton ailleurs', () => {
    http.get('/autre/ressource').subscribe();
    expect(serveur.expectOne('/autre/ressource').request.headers.has('Authorization')).toBe(false);
  });

  it('ferme la session et renvoie à la connexion sur un 401', () => {
    const router = TestBed.inject(Router);
    const navigation = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.get('/api/moi').subscribe({ error: () => undefined });

    serveur.expectOne('/api/moi').flush({ detail: 'Jeton expiré' }, { status: 401, statusText: 'Unauthorized' });

    expect(session.estConnecte()).toBe(false);
    expect(navigation).toHaveBeenCalledWith(['/connexion']);
  });
});
