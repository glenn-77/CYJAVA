package model;

import service.CritereRecherche;
import service.LienService;

import java.util.*;

/**
 * Represents a genealogical tree containing people and their familial relationships.
 * Each tree has a single owner and supports adding, deleting, and querying persons.
 */
public class ArbreGenealogique {

    /** List of all persons (nodes) in the tree */
    private List<Personne> noeuds;
    /** The owner of the genealogical tree */
    private Personne proprietaire;

    /**
     * Constructor initializing the genealogical tree with its owner.
     * @param proprietaire The owner of the tree.
     */
    public ArbreGenealogique(Personne proprietaire) {
        this.proprietaire = proprietaire;
        this.noeuds = new ArrayList<>();
    }

    /** Returns the list of people in the tree. */
    public List<Personne> getNoeuds() {
        return noeuds;
    }

    /** Returns the owner of the tree. */
    public Personne getProprietaire() {
        return proprietaire;
    }

    /**
     * Attempts to add a family link between two people.
     * @param demandeur The person requesting the link.
     * @param personne The target person.
     * @param lien The type of familial link.
     * @return true if the link is added or the request is sent.
     */
    public boolean ajouterLien(Personne demandeur, Personne personne, LienParente lien) {
        if (!noeuds.contains(demandeur)) {
            System.out.println("Le demandeur ne fait pas partie de l'arbre.");
            return false;
        }
        if (!noeuds.contains(personne) && !demandeur.isEstInscrit()) {
            noeuds.add(personne);
            demandeur.ajouterLien(personne, lien);
            System.out.println("Lien ajouté!");
            return true;
        }
        if (!noeuds.contains(personne) && demandeur.isEstInscrit()) {
            System.out.println("Demande de lien envoyée");
            LienService.envoyerDemandeLien(demandeur, personne, lien);
            return false;
        }
        System.out.println(personne.getPrenom() + personne.getNom() + " est déjà dans l'arbre.");
        return false;
    }

    /**
     * Removes a person from the tree. If the person is registered, a request must be approved.
     * @param personne The person to remove.
     * @return true if removed, false otherwise.
     */
    public boolean supprimerNoeud(Personne personne) {
        if (personne.isEstInscrit()) {
            System.out.println("Approbation de la personne inscrite en attente de validation.");
            LienService.demandeSuppressionLien(this.proprietaire, personne, this.proprietaire.getLiens().get(personne));
            return false;
        }
        this.proprietaire.supprimerLien(personne);
        System.out.println("Suppression validée");
        return noeuds.remove(personne);
    }

    /**
     * Displays the tree in a textual form.
     */
    public void afficherTexte() {
        for (Personne p : noeuds) {
            System.out.println(p.getNom() + " (" + p.getLien() + ")");
        }
    }

    /**
     * Checks date-of-birth-based consistency (e.g., parents must be older).
     */
    public void verifierCoherence() {
        Set<LienParente> liensAscendants = Set.of(
                LienParente.PERE, LienParente.MERE,
                LienParente.GRAND_PERE, LienParente.GRAND_MERE,
                LienParente.ARRIERE_GRAND_PERE, LienParente.ARRIERE_GRAND_MERE
        );
        Set<LienParente> liensDescendants = Set.of(
                LienParente.FILS, LienParente.FILLE,
                LienParente.PETIT_FILS, LienParente.PETITE_FILLE
        );

        for (Personne p : noeuds) {
            for (Map.Entry<Personne, LienParente> entry : p.getLiens().entrySet()) {
                Personne autre = entry.getKey();
                LienParente lien = entry.getValue();

                if (p.getDateNaissance() == null || autre.getDateNaissance() == null) continue;

                if (liensAscendants.contains(lien) && !autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                    System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né après " + p.getNom());
                } else if (liensDescendants.contains(lien) && autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                    System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né avant " + p.getNom());
                }
            }
        }
    }

    /**
     * Returns persons visible to a given viewer, based on visibility settings.
     * @param demandeur The person requesting the view.
     * @return A list of visible persons.
     */
    public List<Personne> afficherArbrePour(Personne demandeur) {
        List<Personne> visibles = new ArrayList<>();
        for (Personne p : noeuds) {
            if (p.estVisiblePar(demandeur) || p == demandeur.getPere() || p == demandeur.getMere()) {
                visibles.add(p);
            }
        }
        return visibles;
    }

    /**
     * Checks whether a person exists in the tree.
     * @param personne The person to check.
     * @return true if present, false otherwise.
     */
    public boolean contient(Personne personne) {
        return noeuds.contains(personne);
    }

    /**
     * Builds familial relationships between all persons in the tree.
     */
    public void construireLiensFamiliaux() {
        for (Personne p : noeuds) {
            Personne pere = p.getPere();
            Personne mere = p.getMere();

            if (pere != null && noeuds.contains(pere)) {
                pere.addEnfant(p);
                p.setGeneration(pere.getGeneration() + 1);
            }

            if (mere != null && noeuds.contains(mere)) {
                mere.addEnfant(p);
                if (p.getGeneration() == 0) {
                    p.setGeneration(mere.getGeneration() + 1);
                }
            }

            // Vérifier les parents manquants
            if (pere == null && mere != null && !noeuds.contains(mere)) {
                System.out.println("⚠️ Erreur : lien enfant " + p.getNom() + " ignoré faute de parent dans l'arbre.");
            }
        }
    }

    /**
     * Modifies the information of a given node (Person).
     * @param personne The person whose information needs to be updated.
     * @param nouvellesInfos A map of field names and their new values.
     * @return true if the person exists in the tree and was modified, false otherwise.
     */
    public boolean modifierNoeud(Personne personne, Map<String, String> nouvellesInfos) {
        if (!noeuds.contains(personne)) {
            System.out.println("❌ La personne n'existe pas dans l'arbre.");
            return false;
        }

        // Modifier les informations selon les clés du map
        if (nouvellesInfos.containsKey("email")) {
            personne.getCompte().setEmail(nouvellesInfos.get("email"));
        }
        if (nouvellesInfos.containsKey("adresse")) {
            personne.getCompte().setAdresse(nouvellesInfos.get("adresse"));
        }
        if (nouvellesInfos.containsKey("telephone")) {
            personne.getCompte().setTelephone(nouvellesInfos.get("telephone"));
        }

        System.out.println("✅ Informations mises à jour pour " + personne.getPrenom() + " " + personne.getNom());
        return true;
    }
}