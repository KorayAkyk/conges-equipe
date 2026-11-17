import { TestBed } from '@angular/core/testing';
import { ReponseConnexion } from './modeles';
import { SessionService } from './session';

const reponse = (role: 'EMPLOYE' | 'MANAGER' | 'RH', expiration = '2999-01-01T00:00:00Z'): ReponseConnexion => ({
  jeton: 'jeton-de-test',
  expiration,
  utilisateur: { id: 1, email: 'a@exemple.test', nomComplet: 'Alice', role, equipeId: 1, equipeNom: 'Dev' },
});

describe('SessionService', () => {
  beforeEach(() => sessionStorage.clear());

  it('ouvre puis ferme une session', () => {
    const session = TestBed.inject(SessionService);
    session.ouvrir(reponse('EMPLOYE'));
    expect(session.estConnecte()).toBe(true);
    expect(session.jeton()).toBe('jeton-de-test');
    expect(session.peutDecider()).toBe(false);

    session.fermer();
    expect(session.estConnecte()).toBe(false);
    expect(sessionStorage.length).toBe(0);
  });

  it('donne le droit de décider aux managers et aux RH', () => {
    const session = TestBed.inject(SessionService);
    session.ouvrir(reponse('MANAGER'));
    expect(session.peutDecider()).toBe(true);
  });

  it('reprend une session enregistrée mais ignore une session expirée', () => {
    sessionStorage.setItem('conges.session', JSON.stringify(reponse('RH', '2000-01-01T00:00:00Z')));
    expect(TestBed.inject(SessionService).estConnecte()).toBe(false);
  });
});
