package dao;

import model.ArbreGenealogique;
import model.Compte;
import model.Genre;
import model.Personne;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * DAO class to handle queries from the CSV-based user database.
 */
public class UserDAO {

    private static final String CSV_PATH = "ressources/utilisateurs.csv";

    /**
     * Searches for a person by their social security number.
     * @param nss The NSS to look for.
     * @return The matching Personne object or null if not found.
     */
    public static Personne chercherParNSS(String nss) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && nss.equals(p.getNss())) {
                    return p;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Searches for persons by name and birthdate.
     * @param nom The last name.
     * @param prenom The first name.
     * @return Set of matching persons.
     */
    public static Set<Personne> chercherParNomEtPrenom(String nom, String prenom) {
        Set<Personne> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && p.getNom().equalsIgnoreCase(nom) && p.getPrenom().equals(prenom)) {
                    results.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Retrieves all family members linked to a person by NSS.
     * @param nss The social security number.
     * @return Set of family members.
     */
    public static Set<Personne> membresFamilleParNSS(String nss) {
        Set<Personne> family = new HashSet<>();
        Personne target = chercherParNSS(nss);
        if (target == null) return family;

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && !p.equals(target)) {
                    if (p.getLiens().containsKey(target) || target.getLiens().containsKey(p)) {
                        family.add(p);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return family;
    }

    /**
     * Retrieves the entire family tree of a person by recursively exploring links.
     * @param nss The person's NSS.
     * @return Set representing the full tree.
     */
    public static Set<Personne> arbreCompletParNSS(String nss) {
        Personne root = chercherParNSS(nss);
        if (root == null) return new HashSet<>();

        Set<Personne> tree = new HashSet<>();
        tree.add(root);
        collecterLiens(root, tree);
        return tree;
    }

    /**
     * Helper method to traverse and collect linked persons recursively.
     * @param person Current person.
     * @param tree Aggregated list of connected persons.
     */
    private static void collecterLiens(Personne person, Set<Personne> tree) {
        for (Personne relative : person.getLiens().keySet()) {
            if (!tree.contains(relative)) {
                tree.add(relative);
                collecterLiens(relative, tree);
            }
        }
    }

    /**
     * Parses a CSV line into a Personne object.
     * @param line The CSV line.
     * @return A Personne object or null if invalid.
     */
    private static Personne construireDepuisLigne(String line) {
        try {
            String[] fields = line.split(",");
            if (fields.length < 14) return null;
            String nss = fields[0];
            String nom = fields[1];
            String prenom = fields[2];
            LocalDate date = LocalDate.parse(fields[3]);
            Genre genre = Genre.valueOf(fields[10].toUpperCase());
            String nationalite = fields[4];
            String carteIdentite = fields[5];
            String codePrive = fields[9];

            String email = fields[6].trim();
            String telephone = fields[7].trim();
            String adresse = fields[8].trim();
            String login = fields[11].trim();
            String motDePasse = fields[12].trim();
            Compte compte = new Compte(login, motDePasse, email, telephone, adresse);

            Personne personne = new Personne (nss, prenom, nom, date, nationalite, carteIdentite,
                    codePrive, genre, compte, null);
            personne.setArbre(new ArbreGenealogique(personne));
            return personne;
        } catch (Exception e) {
            return null;
        }
    }
}
