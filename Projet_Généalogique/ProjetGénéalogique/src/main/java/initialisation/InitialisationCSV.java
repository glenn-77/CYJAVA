package initialisation;

import dao.UserDAO;
import entites.*;
import entites.enums.Genre;
import entites.enums.LienParente;
import entites.enums.TypeDemande;
import service.DemandeAdminService.DemandeAdmin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Class to initialize people and their relationships from a CSV file.
 */
public class InitialisationCSV {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
                if (values.length < 20) {
                    System.err.println("Ligne mal formatée (moins de 20 colonnes), ignorée : " + line);
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

                    boolean premiereConnexionBool = premiereConnexion.equals("true");
                    boolean inscritBool = inscrit.equals("true");
                    LocalDate dateNaissance = LocalDate.parse(dateStr, DATE_FORMAT);
                    Genre genre = Genre.valueOf(genreStr);

                    if (urlPhoto.isEmpty()) {
                        urlPhoto = "images/default.png";
                    }

                    // Création de l'objet Compte et Personne
                    Compte compte = new Compte(login, motDePasse, email, telephone, adresse);
                    compte.setPremiereConnexion(premiereConnexionBool);
                    Personne personne = new Personne(nss, prenom, nom, dateNaissance, nationalite, carteIdentite,
                            codePrive, genre, compte, null);
                    personne.setEstInscrit(inscritBool);
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

        // Deuxième passe pour lier les parents et enfants
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
                    DemandeAdmin d = new DemandeAdmin(demandeur, cible, lien, type);
                    demandes.add(d);
                } catch (Exception e) {
                    System.err.println("Erreur sur la ligne : " + line + " -> " + e.getMessage());
                }
            }
        }
        return demandes;
    }
}