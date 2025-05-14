package model;

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
            return true;
        }
        if (!noeuds.contains(personne) && demandeur.isEstInscrit()) {
            demandeur.demanderLien(personne, lien);

        }
        return true;
    }

    /**
     * Supprimer un nœud de l’arbre
     */
    public boolean supprimerNoeud(Personne personne) {
        if (personne.isEstInscrit()) {
            System.out.println("Suppression impossible sans approbation de la personne inscrite.");
            return false;
        }
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
        for (Personne p : noeuds) {
            for (Map.Entry<Personne, LienParente> entry : p.getLiens().entrySet()) {
                Personne autre = entry.getKey();
                LienParente lien = entry.getValue();

                // Ex : un parent ne peut pas être plus jeune que son enfant
                if (lien == LienParente.PERE || lien == LienParente.MERE || lien == LienParente.GRAND_PERE || lien == LienParente.GRAND_MERE || lien == LienParente.ARRIERE_GRAND_MERE || lien == LienParente.ARRIERE_GRAND_PERE || lien == LienParente.BELLE_MERE || lien == LienParente.BEAU_PERE) {
                    if (autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                        System.out.println("Incohérence détectée entre " + p.getNom() + " et " + autre.getNom());
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

