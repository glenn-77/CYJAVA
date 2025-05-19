package dao;

import entites.ArbreGenealogique;
import entites.Compte;
import entites.Genre;
import entites.Personne;

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

    private static final String CSV_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";

    /**
     * Searches for a person by their social security number.
     * @param nss The NSS to look for.
     * @return The matching Personne object or null if not found.
     */
    public static Personne chercherParNSS(String nss) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
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
     * Searches for persons by name.
     * @param fragment The fragment to look for in the name.
     * @return Set of matching persons.
     */
    public static Set<Personne> chercherParNomContient(String fragment) {
        Set<Personne> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && p.getNom().toLowerCase().contains(fragment.toLowerCase())) {
                    results.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Searches for persons by the first name.
     * @param fragment The fragment to look for in the name.
     * @return Set of matching persons.
     */
    public static Set<Personne> chercherParPrenomContient(String fragment) {
        Set<Personne> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && p.getPrenom().toLowerCase().contains(fragment.toLowerCase())) {
                    results.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Searches for persons by NSS.
     * @param fragment The fragment to look for in the NSS.
     * @return Set of matching persons.
     */
    public static Set<Personne> chercherParNSSContient(String fragment) {
        Set<Personne> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && p.getNss().toLowerCase().contains(fragment.toLowerCase())) {
                    results.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Searches for persons by first name.
     * @param prenom The first name.
     * @return Set of matching persons.
     */
    public static Set<Personne> chercherParPrenom(String prenom) {
        Set<Personne> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && p.getPrenom().equalsIgnoreCase(prenom)) {
                    results.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Searches for persons by name.
     * @param nom The last name.
     * @return Set of matching persons.
     */
    public static Set<Personne> chercherParNom(String nom) {
        Set<Personne> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Personne p = construireDepuisLigne(line);
                if (p != null && p.getNom().trim().equalsIgnoreCase(nom.trim())) {
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
            reader.readLine();
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
            String prenom = fields[1];
            String nom = fields[2];
            LocalDate date = LocalDate.parse(fields[3]);
            Genre genre = Genre.valueOf(fields[12].toUpperCase());
            String nationalite = fields[4];
            String carteIdentite = fields[5];
            String codePrive = fields[9];

            String email = fields[6].trim();
            String telephone = fields[7].trim();
            String adresse = fields[8].trim();
            String login = fields[13].trim();
            String motDePasse = fields[14].trim();
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
