package service;

import entites.ArbreGenealogique;
import entites.Personne;
import initialisation.InitialisationCSV;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class responsible for managing the global list of genealogical trees.
 * Supports loading from a CSV file and accessing or adding trees programmatically.
 */
public class GlobalTreesManager {

    private static final List<ArbreGenealogique> arbres = new ArrayList<>();

    /**
     * Loads genealogical trees from the 'utilisateurs.csv' file.
     * Each user loaded from the file is checked for an associated tree,
     * which is added to the global list if not already present.
     */
    public static void chargerArbresDepuisCSV() {
        System.out.println("Chargement des utilisateurs depuis le fichier CSV...");
        InitialisationCSV loader = new InitialisationCSV();

        try (InputStream inputStream = GlobalTreesManager.class.getClassLoader().getResourceAsStream("utilisateurs.csv")) {
            if (inputStream == null) {
                throw new IOException("Fichier utilisateurs.csv introuvable.");
            }

            // Charger tous les utilisateurs depuis le fichier
            List<Personne> utilisateurs = loader.chargerUtilisateurs(inputStream);

            // Ajouter chaque arbre généalogique associé aux utilisateurs
            for (Personne personne : utilisateurs) {
                ArbreGenealogique arbre = personne.getArbre();
                if (arbre != null && !arbres.contains(arbre)) {
                    arbres.add(arbre);
                }
            }

            System.out.println("✅ Chargement terminé. Nombre total d'arbres : " + arbres.size());

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des arbres depuis CSV : " + e.getMessage());
        }
    }

    /**
     * Returns the global list of genealogical trees.
     *
     * @return a list of {@link ArbreGenealogique}
     */
    public static List<ArbreGenealogique> getArbres() {
        return arbres;
    }

    /**
     * Adds a genealogical tree to the global list if not already present.
     *
     * @param arbre the tree to add
     */
    public static void ajouterArbre(ArbreGenealogique arbre) {
        if (arbre != null && !arbres.contains(arbre)) {
            arbres.add(arbre);
            System.out.println("✅ Nouveau arbre ajouté : " +
                    (arbre.getProprietaire() != null ? arbre.getProprietaire().getPrenom() + " " + arbre.getProprietaire().getNom() : "Inconnu"));
        }
    }
}