package service;

import model.ArbreGenealogique;
import service.CoherenceVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire global des arbres généalogiques dans l'application.
 */
public class GlobalTreesManager {

    private static final List<ArbreGenealogique> arbres = new ArrayList<>();

    /**
     * Retourne tous les arbres généalogiques.
     *
     * @return Une liste d'arbres généalogiques.
     */
    public static List<ArbreGenealogique> getArbres() {
        System.out.println("Nombre total d'arbres chargés : " + arbres.size());
        for (ArbreGenealogique arbre : arbres) {
            System.out.println("Arbre du propriétaire : " + (arbre.getProprietaire() != null ?
                    arbre.getProprietaire().getNom() : "Inconnu"));
        }
        return arbres;
    }

    /**
     * Ajoute un nouvel arbre généalogique. Ne l'ajoute que s'il n'existe pas déjà pour le même propriétaire.
     *
     * @param arbre L'arbre généalogique à ajouter.
     */
    public static void ajouterArbre(ArbreGenealogique arbre) {
        if (arbre == null || arbre.getProprietaire() == null || arbre.getNoeuds().isEmpty()) {
            System.out.println("⚠️ Arbre non valide ou sans propriétaire. Il ne sera pas ajouté.");
            return;
        }

        // Vérifier la cohérence de l'arbre (avec CoherenceVerifier)
        CoherenceVerifier.verifierCoherence(arbre);

        // Vérifier si un arbre avec le même propriétaire existe déjà
        for (ArbreGenealogique existant : arbres) {
            if (existant.getProprietaire().equals(arbre.getProprietaire())) {
                System.out.println("⚠️ Un arbre pour ce propriétaire (" + arbre.getProprietaire().getNom() + ") existe déjà.");
                return;
            }
        }

        arbres.add(arbre);
        System.out.println("✅ Arbre ajouté pour le propriétaire : " + arbre.getProprietaire().getNom());
    }

    /**
     * Efface tous les arbres généalogiques (utile pour réinitialisation).
     */
    public static void effacerTousLesArbres() {
        arbres.clear();
    }
}