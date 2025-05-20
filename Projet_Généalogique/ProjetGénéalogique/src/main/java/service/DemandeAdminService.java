package service;

import entites.Personne;
import entites.enums.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DemandeAdminService {

    public static class DemandeAdmin {
        private String id;
        private final Personne demandeur;
        private final Personne cible;
        private final LienParente lien;
        private Statut statut = Statut.EN_ATTENTE;
        private final TypeDemande type;
        private final LocalDate dateCreation = LocalDate.now();

        private static int compteur = 1;

        public DemandeAdmin(Personne demandeur, Personne cible, LienParente lien, TypeDemande type) {
            this.setId(String.format("%03d", compteur++));
            this.demandeur = demandeur;
            this.cible = cible;
            this.lien = lien;
            this.type = type;
        }

        public static int getCompteur() {
            return compteur;
        }

        public static void setCompteur(int nvcompteur) {
            DemandeAdmin.compteur = nvcompteur;
        }

        public Personne getDemandeur() {
            return demandeur;
        }

        public Personne getCible() {
            return cible;
        }

        public LienParente getLien() {
            return lien;
        }

        public Statut getStatut() {
            return statut;
        }

        public void setStatut(Statut statut) {
            this.statut = statut;
        }

        public TypeDemande getType() {
            return type;
        }

        public LocalDate getDateCreation() {
            return dateCreation;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static final Set<DemandeAdmin> demandes = new HashSet<>();

    public static void ajouterDemande(DemandeAdmin demande) {
        demandes.add(demande);
    }

    public static Set<DemandeAdmin> getDemandes() {
        return new HashSet<>(demandes);
    }

    public static void supprimerDemande(DemandeAdmin demande) {
        demandes.remove(demande);
    }

    public static void afficherDemandes() {
        if (demandes.isEmpty()) {
            System.out.println("✅ Aucune demande en attente.");
            return;
        }
        System.out.println("📨 Demandes en attente :");
        for (int i = 0; i < demandes.size(); i++) {
            DemandeAdmin d = new ArrayList<>(demandes).get(i);
            if (d.type == TypeDemande.AJOUT_LIEN) System.out.printf("[%d] %s demande un lien '%s' avec %s\n",
                    i + 1,
                    d.getDemandeur().getNom(),
                    d.getLien(),
                    d.getCible().getNom());
            if (d.type == TypeDemande.SUPPRESSION_LIEN) System.out.printf("[%d] %s demande une suppression du lien '%s' avec %s\n",
                    i + 1,
                    d.getDemandeur().getNom(),
                    d.getLien(),
                    d.getCible().getNom());
            if (d.type == TypeDemande.MODIFICATION_INFO) System.out.printf("[%d] %s souhaite modifier les infos de %s\n",
                    i + 1,
                    d.getDemandeur().getNom(),
                    d.getCible().getNom());
            if (d.type == TypeDemande.AJOUT_PERSONNE) System.out.printf("[%d] %s demande à ajouter %s avec un lien '%s'\n",
                    i + 1,
                    d.getDemandeur().getNom(),
                    d.getCible().getNom(),
                    d.getLien());
        }
    }
}
