import { defineConfig, devices } from '@playwright/test';

// Parcours de bout en bout contre l'application complete lancee par docker compose.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  forbidOnly: !!process.env['CI'],
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://localhost:8080',
    locale: 'fr-FR',
    timezoneId: 'Europe/Paris',
    viewport: { width: 1280, height: 820 },
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 820 },
        locale: 'fr-FR',
        timezoneId: 'Europe/Paris',
        // Chromium complet (nouveau mode headless) : contrairement au binaire headless reduit, il
        // applique la langue de l'interface, dont depend le format des champs de date (jj/mm/aaaa).
        channel: 'chromium',
        launchOptions: { args: ['--lang=fr-FR'] },
      },
    },
  ],
});
