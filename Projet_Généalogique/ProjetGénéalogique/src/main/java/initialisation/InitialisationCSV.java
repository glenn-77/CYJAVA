package initialisation;

import dao.UserDAO;
import entites.*;
import entites.enums.*;
import service.DemandeAdminService.DemandeAdmin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Utility class for initializing user and relationship data from CSV files.
 * Handles user creation, parent-child relationships, and loading admin requests.
 */
public class InitialisationCSV {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Loads user data from a CSV file and initializes associated Personne objects.
     * Also sets up parent-child links after the initial load.
     *
     * @param in the input stream of the CSV file
     * @return a list of users parsed from the CSV
     * @throws IOException if an I/O error occurs while reading
     */
    public List<Personne> chargerUtilisateurs(InputStream in) throws IOException {
        List<Personne> utilisateurs = new ArrayList<>();
        Map<String, Personne> mapNssPersonne = new HashMap<>();
        List<String[]> relations = new ArrayList<>();

        // Lecture initiale pour créer les objets Personne
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Ignorer la ligne d'en-tête

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 22) {
                    System.err.println("Ligne mal formatée (moins de 22 colonnes), ignorée : " + line);
                    continue;
                }

                try {
                    String nss = values[0].trim();
                    String prenom = values[1].trim();
                    String nom = values[2].trim();
                    String dateStr = values[3].trim();
                    String nationalite = values[4].trim();
                    String carteIdentite = values[5].trim();
                    String email = values[6].trim();
                    String telephone = values[7].trim();
                    String adresse = values[8].trim();
                    String codePrive = values[9].trim();
                    String nssPere = values[10].trim();
                    String nssMere = values[11].trim();
                    String genreStr = values[12].trim().toUpperCase();
                    String login = values[13].trim();
                    String motDePasse = values[14].trim();
                    String premiereConnexion = values[16].trim();
                    String urlPhoto = values[18].trim();
                    String inscrit = values[19].trim();
                    String valideParAdmin = values[20].trim();
                    String niveauVisibiliteStr = values[21].trim();

                    boolean premiereConnexionBool = premiereConnexion.equals("true");
                    boolean inscritBool = inscrit.equals("true");
                    boolean valideParAdminBool = valideParAdmin.equals("true");
                    LocalDate dateNaissance = LocalDate.parse(dateStr, DATE_FORMAT);
                    Genre genre = Genre.valueOf(genreStr);
                    if (niveauVisibiliteStr.isEmpty() || niveauVisibiliteStr.equals("null")) niveauVisibiliteStr = "PUBLIQUE";
                    NiveauVisibilite niveauVisibilite = NiveauVisibilite.valueOf(niveauVisibiliteStr.toUpperCase());

                    if (urlPhoto.isEmpty()) {
                        urlPhoto = "images/default.png";
                    }

                    Personne personne;

                    if (email.equals("diffoglenn007@gmail.com")) {
                        Admin compte = new Admin(login, motDePasse, email, telephone, adresse);
                        compte.setPremiereConnexion(premiereConnexionBool);
                        personne = new Personne(nss, prenom, nom, dateNaissance, nationalite, carteIdentite,
                                codePrive, genre, compte, null);

                    } else {
                        Compte compte = new Compte(login, motDePasse, email, telephone, adresse);
                        compte.setPremiereConnexion(premiereConnexionBool);
                        personne = new Personne(nss, prenom, nom, dateNaissance, nationalite, carteIdentite,
                                codePrive, genre, compte, null);
                    }

                    personne.setEstInscrit(inscritBool);
                    personne.setValideParAdmin(valideParAdminBool);
                    personne.setNiveauVisibilite(niveauVisibilite);
                    personne.setArbre(new ArbreGenealogique(personne));
                    personne.setUrlPhoto(urlPhoto);

                    utilisateurs.add(personne);
                    mapNssPersonne.put(nss, personne);

                    // Stockage des relations pour la seconde passe
                    relations.add(new String[]{nss, nssPere, nssMere});

                } catch (Exception e) {
                    System.err.println("Erreur sur la ligne : " + line + " -> " + e.getMessage());
                }
            }
        }

        // Link parents and children after creating all Personne objects
        for (String[] relation : relations) {
            String nssEnfant = relation[0];
            String nssPere = relation[1];
            String nssMere = relation[2];

            Personne enfant = mapNssPersonne.get(nssEnfant);
            if (enfant == null) {
                continue;
            }

            if (nssPere != null && !nssPere.isEmpty()) {
                Personne pere = mapNssPersonne.get(nssPere);
                if (pere != null) {
                    enfant.setPere(pere);
                    pere.addEnfant(enfant);

                    // Assurez-vous que le père est dans la liste des utilisateurs
                    if (!utilisateurs.contains(pere)) {
                        utilisateurs.add(pere);
                    }
                } else {
                    System.err.println("Père introuvable pour NSS : " + nssPere);
                }
            }

            if (nssMere != null && !nssMere.isEmpty()) {
                Personne mere = mapNssPersonne.get(nssMere);
                if (mere != null) {
                    enfant.setMere(mere);
                    mere.addEnfant(enfant);

                    // Assurez-vous que la mère est dans la liste des utilisateurs
                    if (!utilisateurs.contains(mere)) {
                        utilisateurs.add(mere);
                    }
                } else {
                    System.err.println("Mère introuvable pour NSS : " + nssMere);
                }
            }
        }

        return utilisateurs;
    }

    /**
     * Loads administrative requests from a CSV file.
     *
     * @param in the input stream of the CSV file
     * @return a set of admin requests parsed from the CSV
     * @throws IOException if an error occurs while reading the file
     */
    public Set<DemandeAdmin> chargerDemandes(InputStream in) throws IOException {
        Set<DemandeAdmin> demandes = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Ignorer la ligne d'en-tête

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 7) {
                    System.err.println("Ligne mal formatée (moins de 7 colonnes), ignorée : " + line);
                    continue;
                }
                try {
                    TypeDemande type = TypeDemande.valueOf(values[1].trim().toUpperCase());
                    String DemandeurNSS = values[2].trim();
                    String CibleNSS = values[3].trim();
                    LienParente lien = LienParente.valueOf(values[5].trim().toUpperCase());

                    Personne demandeur = UserDAO.chercherParNSS(DemandeurNSS);
                    Personne cible = UserDAO.chercherParNSS(CibleNSS);
                    Statut statut = Statut.valueOf(values[6].trim().toUpperCase());
                    DemandeAdmin d = new DemandeAdmin(demandeur, cible, lien, type);
                    d.setStatut(statut);
                    demandes.add(d);
                } catch (Exception e) {
                    System.err.println("Erreur sur la ligne : " + line + " -> " + e.getMessage());
                }
            }
        }
        return demandes;
    }
}