package model;

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
            System.out.println("Lien ajoutée!");
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
            LienService.demandeSuppressionLien(this.proprietaire, personne, this.proprietaire.getLiens().get(personne) );
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
     * Placeholder for graphical tree display (JavaFX or Swing).
     */
    public void afficherGraphique() {
        // TODO : implémenter vue graphique
    }

    /**
     * Checks date-of-birth-based consistency (e.g., parents must be older).
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
                    if (autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                        System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né avant " + p.getNom());
                    }
                }
            }
        }
    }

    /**
     * Searches persons matching a specific set of criteria.
     * @param critere The criteria to match.
     * @return A list of matching persons.
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
     * Checks whether a person exists in the tree.
     * @param p The person to check.
     * @return true if present, false otherwise.
     */
    public boolean contient(Personne p) {
        return noeuds.contains(p);
    }

    /**
     * Returns persons visible to a given viewer, based on visibility settings.
     * @param demandeur The person requesting the view.
     * @return A list of visible persons.
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
     * Finds persons common to two genealogical trees.
     * @param autre The other genealogical tree.
     * @return A list of shared persons.
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

    /**
     * Edits mutable fields of a person (email, phone, etc.), skipping immutable fields.
     * @param cible The person to edit.
     * @param nouvellesInfos Map of fields to update.
     * @return true if successful, false otherwise.
     */
    public boolean modifierNoeud(Personne cible, Map<String, String> nouvellesInfos) {
        if (!noeuds.contains(cible)) {
            System.out.println("❌ La personne n'existe pas dans l'arbre.");
            return false;
        }

        for (Map.Entry<String, String> entry : nouvellesInfos.entrySet()) {
            String champ = entry.getKey().toLowerCase();
            String valeur = entry.getValue();

            switch (champ) {
                case "adresse":
                    cible.getCompte().setAdresse(valeur);
                    break;
                case "email":
                    cible.getCompte().setEmail(valeur);
                    break;
                case "telephone":
                    cible.getCompte().setTelephone(valeur);
                    break;
                case "login":
                    cible.getCompte().setLogin(valeur);
                    break;
                case "motdepasse":
                    cible.getCompte().setMotDePasse(valeur);
                    break;
                case "numero":
                    cible.getCompte().setNumero(valeur);
                    break;
                default:
                    System.out.println("⚠️ Champ immuable ou inconnu ignoré : " + champ);

            }
        }

        System.out.println("✅ Modification effectuée pour " + cible.getPrenom() + cible.getNom());
        return true;
    }

}

