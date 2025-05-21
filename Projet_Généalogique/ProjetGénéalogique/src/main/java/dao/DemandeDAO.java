package dao;

import entites.Personne;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import entites.enums.LienParente;
import entites.enums.Statut;
import entites.enums.TypeDemande;
import service.DemandeAdminService.DemandeAdmin;

/**
 * DAO class to handle queries from the CSV-based requests database.
 */
public class DemandeDAO {
    private static final String CSV_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/demandes.csv";

    /**
     * Searches for a request by their id.
     * @param fragment The fragment to look for in the request id.
     * @return The matching Personne object or null if not found.
     */
    public static DemandeAdmin chercherParIDContient(String fragment) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                DemandeAdmin d = construireDepuisLigne(line);
                if (d != null && d.getId().toLowerCase().contains(fragment.toLowerCase()) ) {
                    return d;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Searches for requests by demandeur first name.
     * @param fragment The fragment to look for in the request demandeur first name.
     * @return Set of matching requests.
     */
    public static Set<DemandeAdmin> chercherParDemandeurContient(String fragment) {
        Set<DemandeAdmin> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                DemandeAdmin d = construireDepuisLigne(line);
                if (d != null && d.getDemandeur().getPrenom().toLowerCase().contains(fragment.toLowerCase())) {
                    results.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Searches for requests by cible first name.
     * @param fragment The fragment to look for in the request cible first name.
     * @return Set of matching requests.
     */
    public static Set<DemandeAdmin> chercherParCibleContient(String fragment) {
        Set<DemandeAdmin> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                DemandeAdmin d = construireDepuisLigne(line);
                if (d != null && d.getCible().getPrenom().toLowerCase().contains(fragment.toLowerCase())) {
                    results.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Filters for requests by type.
     * @param type The type of request to filter for.
     * @return Set of matching requests.
     */
    public static Set<DemandeAdmin> filtrerParType(TypeDemande type) {
        Set<DemandeAdmin> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                DemandeAdmin d = construireDepuisLigne(line);
                if (d != null && d.getType() == type) {
                    results.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Filters for requests by status.
     * @param statut The status of the request to filter for.
     * @return Set of matching requests.
     */
    public static Set<DemandeAdmin> filtrerParStatut(Statut statut) {
        Set<DemandeAdmin> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                DemandeAdmin d = construireDepuisLigne(line);
                if (d != null && d.getStatut() == statut) {
                    results.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Filters for requests by date.
     * @param date The date of the request to filter for.
     * @return Set of matching requests.
     */
    public static Set<DemandeAdmin> filtrerParDate(LocalDate date) {
        Set<DemandeAdmin> results = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                DemandeAdmin d = construireDepuisLigne(line);
                if (d != null && d.getDateCreation() == date) {
                    results.add(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }


    /**
     * Parses a CSV line into a DemandeAdmin object.
     * @param line The CSV line.
     * @return A DemandeAdmin object or null if invalid.
     */
    private static DemandeAdmin construireDepuisLigne(String line) {
        try {
            String[] fields = line.split(",");
            if (fields.length < 7) return null;

            TypeDemande type = TypeDemande.valueOf(fields[1].toUpperCase());
            String demandeurNSS = fields[2];
            String cibleNSS = fields[3];
            LienParente lien = LienParente.valueOf(fields[5].toString());

            Personne demandeur = UserDAO.chercherParNSS(demandeurNSS);
            Personne cible = UserDAO.chercherParNSS(cibleNSS);
            if (demandeur == null || cible == null) return null;
            DemandeAdmin d = new DemandeAdmin(demandeur, cible, lien, type);
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
