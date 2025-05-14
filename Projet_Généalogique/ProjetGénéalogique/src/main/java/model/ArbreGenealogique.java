package model;

import service.LienService;

import java.util.*;

public class ArbreGenealogique {

    private List<Personne> noeuds;
    private Personne proprietaire;

    // Constructeur
    public ArbreGenealogique(Personne proprietaire) {
        this.proprietaire = proprietaire;
        this.noeuds = new ArrayList<>();
    }

    // Getters
    public List<Personne> getNoeuds() {
        return noeuds;
    }

    public Personne getProprietaire() {
        return proprietaire;
    }

    /**
     * Ajouter un lien de parenté entre deux personnes
     */
    public boolean ajouterLien(Personne demandeur, Personne personne, LienParente lien) {
        if (!noeuds.contains(demandeur)) {
            System.out.println("Le demandeur ne fait pas partie de l'arbre.");
            return false;
        }
        if (!noeuds.contains(personne) && !demandeur.isEstInscrit()) {
            noeuds.add(personne);
            personne.ajouterLien(demandeur, lien);
            System.out.println("Lien ajoutée!");
            return true;
        }
        if (!noeuds.contains(personne) && demandeur.isEstInscrit()) {
            System.out.println("Demande de lien envoyée");
            LienService.envoyerDemandeLien(demandeur, personne, lien);

        }
        return true;
    }

    /**
     * Supprimer un nœud de l’arbre
     */
    public boolean supprimerNoeud(Personne personne) {
        if (personne.isEstInscrit()) {
            System.out.println("Approbation de la personne inscrite en attente de validation.");
            LienService.demandeSuppressionLien(this.proprietaire, personne, this.proprietaire.liensParente.get(personne) );
            return false;
        }
        this.proprietaire.supprimerLien(personne);
        System.out.println("Suppression validée");
        return noeuds.remove(personne);
    }

    /**
     * Affichage textuel de l’arbre
     */
    public void afficherTexte() {
        for (Personne p : noeuds) {
            System.out.println(p.getNom() + " (" + p.getLien() + ")");
        }
    }

    /**
     * Placeholder pour affichage graphique (à compléter avec JavaFX ou Swing)
     */
    public void afficherGraphique() {
        // TODO : implémenter vue graphique
    }

    /**
     * Vérifie les règles de cohérence de l’arbre
     */
    public void verifierCoherence() {
        Set<LienParente> liensAscendants = Set.of(
                LienParente.PERE, LienParente.MERE,
                LienParente.GRAND_PERE, LienParente.GRAND_MERE,
                LienParente.ARRIERE_GRAND_PERE, LienParente.ARRIERE_GRAND_MERE,
                LienParente.BEAU_PERE, LienParente.BELLE_MERE
        );

        Set<LienParente> liensDescendants = Set.of(
                LienParente.FILS, LienParente.FILLE,
                LienParente.PETIT_FILS, LienParente.PETITE_FILLE,
                LienParente.ARRIERE_PETIT_FILS, LienParente.ARRIERE_PETITE_FILLE,
                LienParente.BEAU_FILS, LienParente.BELLE_FILLE,
                LienParente.NEVEU, LienParente.NIECE
        );

        for (Personne p : noeuds) {
            for (Map.Entry<Personne, LienParente> entry : p.getLiens().entrySet()) {
                Personne autre = entry.getKey();
                LienParente lien = entry.getValue();

                if (p.getDateNaissance() == null || autre.getDateNaissance() == null) continue;

                if (liensAscendants.contains(lien)) {
                    if (!autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                        System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né après " + p.getNom());
                    }
                } else if (liensDescendants.contains(lien)) {
                    if (!autre.getDateNaissance().isAfter(p.getDateNaissance())) {
                        System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né avant " + p.getNom());
                    }
                }
            }
        }
    }

    /**
     * Recherche selon des critères
     */
    public List<Personne> rechercherCriteres(CritereRecherche critere) {
        List<Personne> resultats = new ArrayList<>();
        for (Personne p : noeuds) {
            if (critere.match(p)) {
                resultats.add(p);
            }
        }
        return resultats;
    }

    /**
     * Vérifie si une personne figure dans l’arbre
     */
    public boolean contient(Personne p) {
        return noeuds.contains(p);
    }

    /**
     * Retourne les personnes visibles selon les règles de visibilité
     */
    public List<Personne> afficherArbrePour(Personne demandeur) {
        List<Personne> visibles = new ArrayList<>();
        for (Personne p : noeuds) {
            if (p.estVisiblePar(demandeur)) {
                visibles.add(p);
            }
        }
        return visibles;
    }

    /**
     * Recherche de membres communs entre deux arbres
     */
    public List<Personne> trouverMembresCommuns(ArbreGenealogique autre) {
        List<Personne> communs = new ArrayList<>();
        for (Personne p1 : this.noeuds) {
            for (Personne p2 : autre.noeuds) {
                if (p1.isEstInscrit() && p1.equals(p2)) {
                    communs.add(p1);
                } else if (!p1.isEstInscrit() && !p2.isEstInscrit() && p1.infosIdentiques(p2)) {
                    communs.add(p1);
                }
            }
        }
        return communs;
    }
}

