package fr.congesequipe.domaine;

/** Etats d'une demande de conges. */
public enum StatutDemande {
    EN_ATTENTE,
    VALIDEE,
    REFUSEE,
    ANNULEE;

    /** Une demande vivante compte dans le solde et dans les chevauchements. */
    public boolean estVivante() {
        return this == EN_ATTENTE || this == VALIDEE;
    }
}
