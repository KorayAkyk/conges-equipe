package fr.congesequipe.demo;

import fr.congesequipe.domaine.DecompteJours;
import fr.congesequipe.domaine.Periode;
import fr.congesequipe.domaine.Role;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.persistance.DroitConges;
import fr.congesequipe.persistance.DroitCongesRepository;
import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.EquipeRepository;
import fr.congesequipe.persistance.Utilisateur;
import fr.congesequipe.persistance.UtilisateurRepository;
import fr.congesequipe.service.DemandeService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Donnees de demonstration de l'entreprise fictive Brindille Conseil, creees au premier demarrage
 * avec le profil demo. Les personnes sont inventees ; les dates suivent la date du jour pour que le
 * calendrier soit toujours parlant. Les demandes passent par le service, donc par les vraies regles.
 */
@Component
@Profile("demo")
public class DonneesDemo implements ApplicationRunner {

    /** Mot de passe commun aux comptes de demonstration. Ce n'est pas un secret. */
    public static final String MOT_DE_PASSE_DEMO = "demo-conges-2026";

    private static final Logger LOG = LoggerFactory.getLogger(DonneesDemo.class);

    private final EquipeRepository equipes;
    private final UtilisateurRepository utilisateurs;
    private final DroitCongesRepository droits;
    private final DemandeService demandes;
    private final PasswordEncoder encodeur;
    private final Clock horloge;

    public DonneesDemo(EquipeRepository equipes, UtilisateurRepository utilisateurs, DroitCongesRepository droits,
            DemandeService demandes, PasswordEncoder encodeur, Clock horloge) {
        this.equipes = equipes;
        this.utilisateurs = utilisateurs;
        this.droits = droits;
        this.demandes = demandes;
        this.encodeur = encodeur;
        this.horloge = horloge;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (utilisateurs.count() > 0) {
            return;
        }
        String hash = encodeur.encode(MOT_DE_PASSE_DEMO);
        Equipe dev = equipes.save(new Equipe("Développement"));
        Equipe support = equipes.save(new Equipe("Support client"));

        Utilisateur julie = personne("julie.moreau", "Julie Moreau", Role.MANAGER, dev, hash);
        Utilisateur hugo = personne("hugo.lefevre", "Hugo Lefèvre", Role.EMPLOYE, dev, hash);
        Utilisateur lea = personne("lea.roussel", "Léa Roussel", Role.EMPLOYE, dev, hash);
        Utilisateur theo = personne("theo.marchand", "Théo Marchand", Role.EMPLOYE, dev, hash);
        Utilisateur karim = personne("karim.benali", "Karim Benali", Role.MANAGER, support, hash);
        Utilisateur manon = personne("manon.perrin", "Manon Perrin", Role.EMPLOYE, support, hash);
        Utilisateur lucas = personne("lucas.fabre", "Lucas Fabre", Role.EMPLOYE, support, hash);
        Utilisateur nadia = personne("nadia.leclerc", "Nadia Leclerc", Role.RH, null, hash);
        dev.setManagerId(julie.getId());
        support.setManagerId(karim.getId());

        // Semaine de reference : le lundi dans deux semaines.
        LocalDate lundi = LocalDate.now(horloge).plusWeeks(2).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Long h = demandes.creer(hugo.getId(), TypeConge.CONGES_PAYES, new Periode(lundi, lundi.plusDays(4)),
                false, false, "Vacances en famille").id();
        demandes.valider(julie.getId(), h);
        Long t = demandes.creer(theo.getId(), TypeConge.RTT, ouvres(lundi.plusDays(2), 2), false, false, null).id();
        demandes.valider(julie.getId(), t);
        // En attente : la valider ferait passer l'equipe a 3 absents sur 4 -> avertissement.
        demandes.creer(lea.getId(), TypeConge.CONGES_PAYES, ouvres(lundi.plusDays(2), 2), false, false,
                "Déménagement");
        demandes.creer(theo.getId(), TypeConge.CONGES_PAYES, ouvres(lundi.plusWeeks(3), 3), false, true, null);
        Long r = demandes.creer(lucas.getId(), TypeConge.RTT, ouvres(lundi.plusDays(1), 1), false, false, null).id();
        demandes.refuser(karim.getId(), r, "Astreinte du support prévue ce jour-là.");
        Long m = demandes.creer(manon.getId(), TypeConge.CONGES_PAYES, ouvres(lundi.plusWeeks(1), 5), false, false,
                null).id();
        demandes.valider(karim.getId(), m);
        Long a = demandes.creer(manon.getId(), TypeConge.RTT, ouvres(lundi.plusWeeks(4), 1), true, false, null).id();
        demandes.annuler(manon.getId(), a);
        demandes.creer(julie.getId(), TypeConge.RTT, ouvres(lundi.plusWeeks(2), 1), false, false, null);
        LOG.info("Donnees de demonstration creees : 8 comptes, dont {}", nadia.getEmail());
    }

    private Utilisateur personne(String identifiant, String nom, Role role, Equipe equipe, String hash) {
        Utilisateur u = utilisateurs.save(new Utilisateur(identifiant + "@brindille.test", nom, hash, role, equipe));
        droits.save(new DroitConges(u.getId(), TypeConge.CONGES_PAYES, new BigDecimal("25.0")));
        droits.save(new DroitConges(u.getId(), TypeConge.RTT, new BigDecimal("10.0")));
        return u;
    }

    /** Periode de n jours ouvres a partir du premier jour ouvre a la date donnee ou apres. */
    private static Periode ouvres(LocalDate depuis, int nombre) {
        LocalDate debut = depuis;
        while (!DecompteJours.estOuvre(debut)) {
            debut = debut.plusDays(1);
        }
        LocalDate fin = debut;
        int compte = 1;
        while (compte < nombre) {
            fin = fin.plusDays(1);
            if (DecompteJours.estOuvre(fin)) {
                compte++;
            }
        }
        return new Periode(debut, fin);
    }
}
