import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../../core/api';
import { aujourdhui, libelleStatut, libelleType, messageErreur } from '../../core/format';
import { CalendrierEquipe, Equipe } from '../../core/modeles';
import { Cellule, construireGrille, libelleMois, moisVoisin } from './grille';

@Component({
  selector: 'app-calendrier',
  imports: [FormsModule],
  templateUrl: './calendrier.html',
  styleUrl: './calendrier.css',
})
export class Calendrier implements OnInit {
  private readonly api = inject(Api);

  protected readonly mois = signal(aujourdhui().slice(0, 7));
  protected readonly equipes = signal<Equipe[]>([]);
  protected readonly equipeId = signal<number | undefined>(undefined);
  protected readonly donnees = signal<CalendrierEquipe | null>(null);
  protected readonly erreur = signal<string | null>(null);
  protected readonly grille = computed(() => {
    const donnees = this.donnees();
    return donnees ? construireGrille(donnees) : null;
  });
  protected readonly titreMois = computed(() => libelleMois(this.mois()));

  ngOnInit(): void {
    this.api.equipes().subscribe({ next: (e) => this.equipes.set(e) });
    this.charger();
  }

  protected changerMois(decalage: number): void {
    this.mois.set(moisVoisin(this.mois(), decalage));
    this.charger();
  }

  protected choisirEquipe(id: number): void {
    this.equipeId.set(Number(id));
    this.charger();
  }

  protected infobulle(cellule: Cellule): string {
    if (!cellule.statut || !cellule.type) {
      return '';
    }
    const demi = cellule.demi === 'matin' ? ' (matin)' : cellule.demi === 'apres-midi' ? ' (après-midi)' : '';
    return `${libelleType(cellule.type)} - ${libelleStatut(cellule.statut)}${demi}`;
  }

  private charger(): void {
    this.erreur.set(null);
    this.api.calendrier(this.mois(), this.equipeId()).subscribe({
      next: (c) => {
        this.donnees.set(c);
        this.equipeId.set(c.equipeId);
      },
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }
}
