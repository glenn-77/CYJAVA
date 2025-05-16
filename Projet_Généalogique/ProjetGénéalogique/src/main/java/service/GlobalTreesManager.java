package service;

import model.ArbreGenealogique;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class GlobalTreesManager {

    // Liste contenant tous les arbres généalogiques de toutes les familles
    private static final List<ArbreGenealogique> arbres = new ArrayList<>();

    /**
     * Retourne tous les arbres généalogiques.
     *
     * @return Une liste d'arbres généalogiques.
     */
    public static List<ArbreGenealogique> getArbres() {
        return arbres;
    }

    /**
     * Ajoute un nouvel arbre généalogique.
     *
     * @param arbre L'arbre généalogique à ajouter.
     */
    public static void ajouterArbre(ArbreGenealogique arbre) {
        if (!arbres.contains(arbre)) {
            arbres.add(arbre);
        }
    }

    /**
     * Supprime un arbre généalogique existant.
     *
     * @param arbre L'arbre généalogique à supprimer.
     */
    public static void supprimerArbre(ArbreGenealogique arbre) {
        arbres.remove(arbre);
    }

    /**
     * Efface tous les arbres généalogiques (utile pour réinitialisation).
     */
    public static void effacerTousLesArbres() {
        arbres.clear();
    }


}