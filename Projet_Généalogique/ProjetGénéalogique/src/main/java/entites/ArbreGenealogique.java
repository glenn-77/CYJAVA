package entites;


import java.util.*;

/**
 * Represents a genealogical tree containing people and their familial relationships.
 * Each tree has a single owner and supports adding, deleting, and querying persons.
 */
public class ArbreGenealogique {

    /** List of all persons (nodes) in the tree */
    private Set<Personne> noeuds;
    /** The owner of the genealogical tree */
    private Personne proprietaire;

    /**
     * Constructor initializing the genealogical tree with its owner.
     * @param proprietaire The owner of the tree.
     */
    public ArbreGenealogique(Personne proprietaire) {
        if (proprietaire == null) {
            throw new IllegalArgumentException("Le propriétaire de l'arbre ne peut pas être null.");
        }
        this.proprietaire = proprietaire;
        this.noeuds = new HashSet<>();
        this.noeuds.add(proprietaire); // Ajouter le propriétaire comme premier noeud
    }

    /** Returns the list of people in the tree. */
    public Set<Personne> getNoeuds() {
        return noeuds;
    }

    /** Returns the owner of the tree. */
    public Personne getProprietaire() {
        return proprietaire;
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
    public Set<Personne> afficherArbrePour(Personne demandeur) {
        Set<Personne> visibles = new HashSet<>();
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
    public Set<Personne> trouverMembresCommuns(ArbreGenealogique autre) {
        Set<Personne> communs = new HashSet<>();
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
}