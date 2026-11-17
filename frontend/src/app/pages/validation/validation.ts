import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../../core/api';
import { dateLisible, jours, libelleType, messageErreur } from '../../core/format';
import { Demande } from '../../core/modeles';

/** Demandes en attente que le manager (son equipe) ou les RH (tout le monde) peuvent decider. */
@Component({
  selector: 'app-validation',
  imports: [FormsModule],
  templateUrl: './validation.html',
  styleUrl: './validation.css',
})
export class Validation implements OnInit {
  private readonly api = inject(Api);

  protected readonly demandes = signal<Demande[]>([]);
  protected readonly chargee = signal(false);
  protected readonly message = signal<string | null>(null);
  protected readonly erreur = signal<string | null>(null);
  protected readonly refusEnCours = signal<number | null>(null);
  protected motif = '';

  protected readonly dateLisible = dateLisible;
  protected readonly jours = jours;
  protected readonly libelleType = libelleType;

  ngOnInit(): void {
    this.recharger();
  }

  protected valider(demande: Demande): void {
    this.erreur.set(null);
    this.api.valider(demande.id).subscribe({
      next: (d) => {
        const alerte = d.joursSousEffectif.length
          ? ` Attention : plus de la moitié de l'équipe sera absente (${d.joursSousEffectif.map(dateLisible).join(', ')}).`
          : '';
        this.message.set(`Demande de ${d.demandeurNom} validée.${alerte}`);
        this.recharger();
      },
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }

  protected ouvrirRefus(demande: Demande): void {
    this.refusEnCours.set(demande.id);
    this.motif = '';
  }

  protected confirmerRefus(demande: Demande): void {
    this.erreur.set(null);
    this.api.refuser(demande.id, this.motif).subscribe({
      next: (d) => {
        this.message.set(`Demande de ${d.demandeurNom} refusée.`);
        this.refusEnCours.set(null);
        this.recharger();
      },
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }

  protected alerteEffectif(demande: Demande): string {
    return demande.joursSousEffectif.map(dateLisible).join(', ');
  }

  private recharger(): void {
    this.api.aDecider().subscribe({
      next: (d) => {
        this.demandes.set(d);
        this.chargee.set(true);
      },
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }
}
