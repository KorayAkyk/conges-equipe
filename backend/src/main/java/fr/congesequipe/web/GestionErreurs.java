package fr.congesequipe.web;

import fr.congesequipe.domaine.AccesInterditException;
import fr.congesequipe.domaine.IntrouvableException;
import fr.congesequipe.domaine.RegleMetierException;
import fr.congesequipe.service.IdentifiantsInvalidesException;
import java.net.URI;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Toutes les erreurs sortent au format Problem Details (RFC 9457), avec un message en francais et,
 * pour les regles metier, un code stable que l'interface peut tester.
 */
@RestControllerAdvice
public class GestionErreurs extends ResponseEntityExceptionHandler {

    private static final String BASE = "https://conges-equipe.example.com/erreurs/";

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regleMetier(RegleMetierException e) {
        ProblemDetail probleme = probleme(HttpStatus.UNPROCESSABLE_ENTITY, "Règle métier non respectée",
                e.getMessage(), e.code());
        probleme.setProperty("code", e.code());
        return probleme;
    }

    @ExceptionHandler(AccesInterditException.class)
    ProblemDetail accesInterdit(AccesInterditException e) {
        return probleme(HttpStatus.FORBIDDEN, "Accès refusé", e.getMessage(), "acces-refuse");
    }

    @ExceptionHandler(IntrouvableException.class)
    ProblemDetail introuvable(IntrouvableException e) {
        return probleme(HttpStatus.NOT_FOUND, "Introuvable", e.getMessage(), "introuvable");
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    ProblemDetail identifiants(IdentifiantsInvalidesException e) {
        return probleme(HttpStatus.UNAUTHORIZED, "Connexion refusée", e.getMessage(), "identifiants-invalides");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail conflit(OptimisticLockingFailureException e) {
        return probleme(HttpStatus.CONFLICT, "Modification concurrente",
                "La demande a été modifiée entre-temps. Rechargez la page.", "conflit");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            HttpHeaders headers, HttpStatusCode status, WebRequest requete) {
        ProblemDetail probleme = probleme(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Certains champs sont manquants ou invalides.", "requete-invalide");
        probleme.setProperty("champs", e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .toList());
        return ResponseEntity.badRequest().body(probleme);
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, @Nullable String detail, String type) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setTitle(titre);
        probleme.setType(URI.create(BASE + type.toLowerCase().replace('_', '-')));
        return probleme;
    }
}
