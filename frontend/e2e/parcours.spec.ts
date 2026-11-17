import { expect, Page, test } from '@playwright/test';

// Parcours complet sur les donnees de demonstration (base neuve : docker compose down -v avant de relancer).
// Les captures du README sont prises ici, sur l'application reellement demarree.

const MOT_DE_PASSE = 'demo-conges-2026';
const CAPTURES = 'e2e/captures';

test.describe.configure({ mode: 'serial' });

async function connecter(page: Page, email: string): Promise<void> {
  await page.goto('/connexion');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Mot de passe').fill(MOT_DE_PASSE);
  await page.getByRole('button', { name: 'Se connecter' }).click();
  await expect(page.getByTestId('utilisateur')).toBeVisible();
}

/** Date locale au format AAAA-MM-JJ, decalee d'un nombre de jours puis avancee jusqu'au mardi. */
function mardiDans(jours: number): string {
  const d = new Date();
  d.setDate(d.getDate() + jours);
  while (d.getDay() !== 2) {
    d.setDate(d.getDate() + 1);
  }
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

test('la page de connexion présente les comptes de démonstration', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/connexion$/);
  await expect(page.getByRole('heading', { name: 'Comptes de démonstration' })).toBeVisible();
  await page.screenshot({ path: `${CAPTURES}/connexion.png` });
});

test('un employé dépose une demande de RTT', async ({ page }) => {
  await connecter(page, 'hugo.lefevre@brindille.test');
  await expect(page.getByTestId('solde-CONGES_PAYES')).toContainText('disponibles');

  const debut = mardiDans(70);
  await page.getByLabel('Type').selectOption('RTT');
  await page.getByLabel('Du', { exact: true }).fill(debut);
  await page.getByLabel('Au', { exact: true }).fill(debut);
  await page.getByLabel('Commentaire').fill('Rendez-vous administratif');
  await page.getByRole('button', { name: 'Envoyer la demande' }).click();

  await expect(page.getByRole('status')).toContainText('en attente de validation');
  await expect(page.getByTestId('ligne-demande').filter({ hasText: 'RTT' }).first()).toContainText('En attente');
  await page.screenshot({ path: `${CAPTURES}/mes-demandes.png`, fullPage: true });
});

test('le manager voit l avertissement de sous-effectif et valide la demande', async ({ page }) => {
  await connecter(page, 'julie.moreau@brindille.test');
  await expect(page).toHaveURL(/\/validation$/);

  const lea = page.getByTestId('demande-a-valider').filter({ hasText: 'Léa Roussel' });
  await expect(lea).toContainText("Plus de la moitié de l'équipe serait absente");
  await page.screenshot({ path: `${CAPTURES}/validation.png`, fullPage: true });

  const hugo = page.getByTestId('demande-a-valider').filter({ hasText: 'Hugo Lefèvre' });
  await hugo.getByRole('button', { name: 'Valider' }).click();
  await expect(page.getByRole('status')).toContainText('Hugo Lefèvre validée');
  await expect(hugo).toHaveCount(0);
});

test('le manager refuse une demande avec un motif', async ({ page }) => {
  await connecter(page, 'julie.moreau@brindille.test');
  const theo = page.getByTestId('demande-a-valider').filter({ hasText: 'Théo Marchand' });
  await theo.getByRole('button', { name: 'Refuser' }).click();
  await theo.getByLabel('Motif du refus (obligatoire)').fill('Livraison client prévue cette semaine');
  await theo.getByRole('button', { name: 'Confirmer le refus' }).click();
  await expect(page.getByRole('status')).toContainText('Théo Marchand refusée');
});

test('l employé retrouve la décision et son historique', async ({ page }) => {
  await connecter(page, 'hugo.lefevre@brindille.test');
  const ligne = page.getByTestId('ligne-demande').filter({ hasText: 'RTT' }).first();
  await expect(ligne).toContainText('Validée');
  await ligne.getByRole('button', { name: 'Historique' }).click();
  await expect(page.locator('.historique')).toContainText('Validée par Julie Moreau');
});

test('le calendrier montre les absences de l équipe', async ({ page }) => {
  await connecter(page, 'julie.moreau@brindille.test');
  await page.getByRole('link', { name: 'Calendrier' }).click();
  await expect(page.getByRole('heading', { name: "Calendrier de l'équipe" })).toBeVisible();

  const cible = new Date();
  cible.setDate(cible.getDate() + 16);
  const ecart = (cible.getFullYear() - new Date().getFullYear()) * 12 + cible.getMonth() - new Date().getMonth();
  for (let i = 0; i < ecart; i++) {
    await page.getByRole('button', { name: 'Mois suivant' }).click();
  }
  await expect(page.getByRole('rowheader', { name: 'Hugo Lefèvre' })).toBeVisible();
  await expect(page.locator('td.validee').first()).toBeVisible();
  await page.screenshot({ path: `${CAPTURES}/calendrier.png`, fullPage: true });
});

test('les RH décident de la demande d un manager', async ({ page }) => {
  await connecter(page, 'nadia.leclerc@brindille.test');
  await expect(page.getByTestId('demande-a-valider').filter({ hasText: 'Julie Moreau' })).toBeVisible();
});

test('la documentation Swagger UI est servie', async ({ page }) => {
  await page.goto('/swagger-ui.html');
  await expect(page.locator('.swagger-ui .info .title')).toContainText('API conges-equipe');
  await page.screenshot({ path: `${CAPTURES}/swagger.png` });
});
