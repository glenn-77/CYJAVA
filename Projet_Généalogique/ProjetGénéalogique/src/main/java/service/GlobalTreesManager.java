package service;

import entites.ArbreGenealogique;
import entites.Personne;
import initialisation.InitialisationCSV;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la liste globale des arbres généalogiques.
 */
public class GlobalTreesManager {

    private static final List<ArbreGenealogique> arbres = new ArrayList<>();

    /**
     * Charge les arbres généalogiques depuis le fichier utilisateurs.csv.
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
     * Retourne tous les arbres généalogiques.
     *
     * @return Une liste d'arbres généalogiques.
     */
    public static List<ArbreGenealogique> getArbres() {
        return arbres;
    }

    /**
     * Ajoute un arbre généalogique à la liste globale.
     *
     * @param arbre L'arbre à ajouter.
     */
    public static void ajouterArbre(ArbreGenealogique arbre) {
        if (arbre != null && !arbres.contains(arbre)) {
            arbres.add(arbre);
            System.out.println("✅ Nouveau arbre ajouté : " +
                    (arbre.getProprietaire() != null ? arbre.getProprietaire().getPrenom() + " " + arbre.getProprietaire().getNom() : "Inconnu"));
        }
    }
}