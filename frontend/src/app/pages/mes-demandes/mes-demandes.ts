import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Api } from '../../core/api';
import { aujourdhui, dateLisible, jours, libelleStatut, libelleType, messageErreur } from '../../core/format';
import { Demande, Evenement, Solde, TypeConge } from '../../core/modeles';

@Component({
  selector: 'app-mes-demandes',
  imports: [ReactiveFormsModule],
  templateUrl: './mes-demandes.html',
  styleUrl: './mes-demandes.css',
})
export class MesDemandes implements OnInit {
  private readonly api = inject(Api);

  protected readonly soldes = signal<Solde[]>([]);
  protected readonly demandes = signal<Demande[]>([]);
  protected readonly erreur = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);
  protected readonly historiques = signal<Record<number, Evenement[]>>({});
  protected readonly formulaire = inject(FormBuilder).nonNullable.group({
    type: ['CONGES_PAYES' as TypeConge, Validators.required],
    dateDebut: ['', Validators.required],
    dateFin: ['', Validators.required],
    debutApresMidi: [false],
    finMatin: [false],
    commentaire: [''],
  });

  protected readonly dateLisible = dateLisible;
  protected readonly jours = jours;
  protected readonly libelleStatut = libelleStatut;
  protected readonly libelleType = libelleType;

  ngOnInit(): void {
    this.recharger();
  }

  protected deposer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }
    const valeur = this.formulaire.getRawValue();
    this.erreur.set(null);
    this.api.creer({ ...valeur, commentaire: valeur.commentaire.trim() || null }).subscribe({
      next: (d) => {
        this.message.set(`Demande envoyée : ${jours(d.jours)} décomptés, en attente de validation.`);
        this.formulaire.reset();
        this.recharger();
      },
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }

  protected annulable(demande: Demande): boolean {
    return (demande.statut === 'EN_ATTENTE' || demande.statut === 'VALIDEE') && demande.dateDebut > aujourdhui();
  }

  protected annuler(demande: Demande): void {
    this.erreur.set(null);
    this.api.annuler(demande.id).subscribe({
      next: () => {
        this.message.set('Demande annulée.');
        this.recharger();
      },
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }

  protected basculerHistorique(demande: Demande): void {
    const actuels = this.historiques();
    if (actuels[demande.id]) {
      const reste = { ...actuels };
      delete reste[demande.id];
      this.historiques.set(reste);
      return;
    }
    this.api.historique(demande.id).subscribe({
      next: (h) => this.historiques.set({ ...this.historiques(), [demande.id]: h }),
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }

  protected horodatage(iso: string): string {
    return new Date(iso).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' });
  }

  private recharger(): void {
    this.api.profil().subscribe({
      next: (p) => this.soldes.set(p.soldes),
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
    this.api.mesDemandes().subscribe({
      next: (d) => this.demandes.set(d),
      error: (e: unknown) => this.erreur.set(messageErreur(e)),
    });
  }
}
