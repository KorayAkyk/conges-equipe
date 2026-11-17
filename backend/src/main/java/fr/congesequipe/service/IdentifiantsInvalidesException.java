package fr.congesequipe.service;

/** E-mail inconnu ou mot de passe faux. Le message ne dit pas lequel des deux. */
public class IdentifiantsInvalidesException extends RuntimeException {

    public IdentifiantsInvalidesException() {
        super("E-mail ou mot de passe incorrect.");
    }
}
