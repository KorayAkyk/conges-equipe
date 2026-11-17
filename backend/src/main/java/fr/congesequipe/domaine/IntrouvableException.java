package fr.congesequipe.domaine;

/** La ressource demandee n'existe pas. */
public class IntrouvableException extends RuntimeException {

    public IntrouvableException(String message) {
        super(message);
    }
}
