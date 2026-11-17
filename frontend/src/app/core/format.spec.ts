import { HttpErrorResponse } from '@angular/common/http';
import { aujourdhui, dateLisible, jours, libelleStatut, libelleType, messageErreur } from './format';

describe('format', () => {
  it('traduit les statuts et les types', () => {
    expect(libelleStatut('EN_ATTENTE')).toBe('En attente');
    expect(libelleStatut('VALIDEE')).toBe('Validée');
    expect(libelleType('CONGES_PAYES')).toBe('Congés payés');
  });

  it('affiche une date ISO sans glisser de jour', () => {
    const texte = dateLisible('2026-12-08');
    expect(texte).toContain('8 déc');
    expect(texte).toContain('2026');
  });

  it('affiche les jours avec une virgule décimale', () => {
    expect(jours(3.5)).toBe('3,5 j');
    expect(jours(20)).toBe('20 j');
  });

  it('donne la date du jour au format AAAA-MM-JJ', () => {
    expect(aujourdhui(new Date(2026, 0, 5, 23, 30))).toBe('2026-01-05');
  });

  it('lit le detail d un Problem Details', () => {
    const erreur = new HttpErrorResponse({ status: 422, error: { detail: 'Solde insuffisant.' } });
    expect(messageErreur(erreur)).toBe('Solde insuffisant.');
  });

  it('signale un serveur injoignable et une erreur inconnue', () => {
    expect(messageErreur(new HttpErrorResponse({ status: 0 }))).toBe('Le serveur ne répond pas.');
    expect(messageErreur(new Error('x'))).toBe('Une erreur inattendue est survenue.');
  });
});
