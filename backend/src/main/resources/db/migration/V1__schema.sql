-- Schema initial. Flyway est seul maitre du schema : Hibernate ne cree ni ne modifie rien.

create table equipe (
    id         bigserial primary key,
    nom        varchar(100) not null unique,
    manager_id bigint
);

create table utilisateur (
    id           bigserial primary key,
    email        varchar(200) not null unique,
    nom_complet  varchar(150) not null,
    mot_de_passe varchar(100) not null,
    role         varchar(20)  not null check (role in ('EMPLOYE', 'MANAGER', 'RH')),
    equipe_id    bigint references equipe (id)
);

alter table equipe
    add constraint fk_equipe_manager foreign key (manager_id) references utilisateur (id);

create index idx_utilisateur_equipe on utilisateur (equipe_id);

create table droit_conges (
    id             bigserial primary key,
    utilisateur_id bigint       not null references utilisateur (id),
    type           varchar(20)  not null check (type in ('CONGES_PAYES', 'RTT')),
    jours_acquis   numeric(5, 1) not null check (jours_acquis >= 0),
    unique (utilisateur_id, type)
);

create table demande_conge (
    id               bigserial primary key,
    demandeur_id     bigint        not null references utilisateur (id),
    type             varchar(20)   not null check (type in ('CONGES_PAYES', 'RTT')),
    date_debut       date          not null,
    date_fin         date          not null,
    debut_apres_midi boolean       not null default false,
    fin_matin        boolean       not null default false,
    jours            numeric(5, 1) not null check (jours > 0),
    statut           varchar(20)   not null check (statut in ('EN_ATTENTE', 'VALIDEE', 'REFUSEE', 'ANNULEE')),
    motif_refus      varchar(500),
    commentaire      varchar(500),
    cree_le          timestamptz   not null,
    version          bigint        not null default 0,
    check (date_fin >= date_debut),
    check (statut <> 'REFUSEE' or motif_refus is not null)
);

create index idx_demande_demandeur on demande_conge (demandeur_id, statut);
create index idx_demande_periode on demande_conge (date_debut, date_fin);

create table historique_demande (
    id             bigserial primary key,
    demande_id     bigint       not null references demande_conge (id),
    ancien_statut  varchar(20),
    nouveau_statut varchar(20)  not null,
    auteur_id      bigint       not null references utilisateur (id),
    commentaire    varchar(500),
    horodatage     timestamptz  not null
);

create index idx_historique_demande on historique_demande (demande_id);
