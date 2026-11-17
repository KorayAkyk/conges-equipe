import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../core/api';
import { messageErreur } from '../../core/format';
import { SessionService } from '../../core/session';

/** Comptes inventes crees par le profil demo du backend. */
export const COMPTES_DEMO = [
  { email: 'hugo.lefevre@brindille.test', role: 'Employé, équipe Développement' },
  { email: 'julie.moreau@brindille.test', role: 'Manager, équipe Développement' },
  { email: 'nadia.leclerc@brindille.test', role: 'Ressources humaines' },
];
export const MOT_DE_PASSE_DEMO = 'demo-conges-2026';

@Component({
  selector: 'app-connexion',
  imports: [ReactiveFormsModule],
  templateUrl: './connexion.html',
})
export class Connexion {
  private readonly api = inject(Api);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  protected readonly comptes = COMPTES_DEMO;
  protected readonly motDePasseDemo = MOT_DE_PASSE_DEMO;
  protected readonly erreur = signal<string | null>(null);
  protected readonly envoi = signal(false);
  protected readonly formulaire = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', Validators.required],
  });

  protected remplir(email: string): void {
    this.formulaire.setValue({ email, motDePasse: MOT_DE_PASSE_DEMO });
  }

  protected seConnecter(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }
    const { email, motDePasse } = this.formulaire.getRawValue();
    this.envoi.set(true);
    this.erreur.set(null);
    this.api.connexion(email, motDePasse).subscribe({
      next: (reponse) => {
        this.session.ouvrir(reponse);
        void this.router.navigate([reponse.utilisateur.role === 'EMPLOYE' ? '/mes-demandes' : '/validation']);
      },
      error: (e: unknown) => {
        this.erreur.set(messageErreur(e));
        this.envoi.set(false);
      },
    });
  }
}
