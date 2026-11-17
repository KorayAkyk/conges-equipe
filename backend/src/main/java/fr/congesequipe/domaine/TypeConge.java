package fr.congesequipe.domaine;

/** Types d'absence geres. Chaque type a son propre solde. */
public enum TypeConge {
    CONGES_PAYES("Congés payés"),
    RTT("RTT");

    private final String libelle;

    TypeConge(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
