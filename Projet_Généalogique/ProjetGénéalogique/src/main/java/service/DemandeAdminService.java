package service;

import entites.Personne;
import entites.LienParente;

import java.util.ArrayList;
import java.util.List;

public class DemandeAdminService {

    public static class DemandeAdmin {
        private final Personne demandeur;
        private final Personne cible;
        private final LienParente lien;

        public DemandeAdmin(Personne demandeur, Personne cible, LienParente lien) {
            this.demandeur = demandeur;
            this.cible = cible;
            this.lien = lien;
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
    }

    private static final List<DemandeAdmin> demandes = new ArrayList<>();

    public static void ajouterDemande(DemandeAdmin demande) {
        demandes.add(demande);
    }

    public static List<DemandeAdmin> getDemandes() {
        return new ArrayList<>(demandes);
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
            DemandeAdmin d = demandes.get(i);
            System.out.printf("[%d] %s demande un lien '%s' avec %s\n",
                    i + 1,
                    d.getDemandeur().getNom(),
                    d.getLien(),
                    d.getCible().getNom());
        }
    }

    public static DemandeAdmin getDemandeParIndex(int index) {
        if (index >= 0 && index < demandes.size()) {
            return demandes.get(index);
        }
        return null;
    }
}
