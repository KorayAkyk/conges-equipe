package fr.congesequipe.domaine;

/** Une regle metier refuse l'operation. Le code est stable et lisible par le client. */
public class RegleMetierException extends RuntimeException {

    private final String code;

    public RegleMetierException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
