package fr.congesequipe.domaine;

/** L'utilisateur est connu mais n'a pas le droit d'agir sur cette ressource. */
public class AccesInterditException extends RuntimeException {

    public AccesInterditException(String message) {
        super(message);
    }
}
