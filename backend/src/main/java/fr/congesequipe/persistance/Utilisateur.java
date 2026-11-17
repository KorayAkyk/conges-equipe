package fr.congesequipe.persistance;

import fr.congesequipe.domaine.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "nom_complet", nullable = false, length = 150)
    private String nomComplet;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    protected Utilisateur() {
    }

    public Utilisateur(String email, String nomComplet, String motDePasse, Role role, Equipe equipe) {
        this.email = email;
        this.nomComplet = nomComplet;
        this.motDePasse = motDePasse;
        this.role = role;
        this.equipe = equipe;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    /** Vrai si cet utilisateur est le manager de l'equipe de l'autre. */
    public boolean manage(Utilisateur autre) {
        return role == Role.MANAGER && autre.getEquipe() != null && id.equals(autre.getEquipe().getManagerId());
    }
}
