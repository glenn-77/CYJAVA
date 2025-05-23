package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import entites.ArbreGenealogique;
import entites.Compte;
import entites.Personne;
import initialisation.InitialisationCSV;
import entites.enums.Genre;
import service.DemandeAdminService.DemandeAdmin;

/**
 * Authentication and user management service.
 * Handles user registration, login, administrative requests, and genealogy tree initialization from CSV files.
 */
public class AuthService {

    private final Map<String, Personne> utilisateurs; // Email -> Personne
    private final List<ArbreGenealogique> arbres;// Liste des arbres généalogiques
    private final Set<DemandeAdmin> demandesAdmins;

    private static final String UTILISATEURS_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";
    private static final String DEMANDE_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/demandes.csv";

    /**
     * Default constructor that loads users, counters, and requests from CSV files at startup.
     */
    public AuthService() {
        this.utilisateurs = new HashMap<>();
        this.arbres = new ArrayList<>();
        this.demandesAdmins = new HashSet<>();
        try {
            chargerUtilisateursDepuisCSV();
            try (BufferedReader reader = new BufferedReader(new FileReader("Projet_Généalogique/ProjetGénéalogique/ressources/compteur.txt"))) {
                String ligne = reader.readLine();
                if (ligne != null) {
                    int valeur = Integer.parseInt(ligne.trim());
                    Compte.setCompteur(valeur);
                }
            } catch (IOException e) {
                System.out.println("Aucun fichier de compteur trouvé, valeur par défaut utilisée.");
            }
            chargerDemandesDepuisCSV();
            try (BufferedReader reader = new BufferedReader(new FileReader("Projet_Généalogique/ProjetGénéalogique/ressources/compteurD.txt"))) {
                String ligne = reader.readLine();
                if (ligne != null) {
                    int valeur = Integer.parseInt(ligne.trim());
                    Compte.setCompteur(valeur);
                }
            } catch (IOException e) {
                System.out.println("Aucun fichier de compteur trouvé, valeur par défaut utilisée.");
            }
            chargerArbres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads users from the `utilisateurs.csv` file and indexes them by their NSS.
     */
    private void chargerUtilisateursDepuisCSV() throws IOException {
        InitialisationCSV loader = new InitialisationCSV();
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("utilisateurs.csv")) {
            if (inputStream == null) {
                throw new IOException("Fichier utilisateurs.csv introuvable dans les ressources.");
            }
            List<Personne> utilisateursList = loader.chargerUtilisateurs(inputStream);
            for (Personne personne : utilisateursList) {
                utilisateurs.put(personne.getNss(), personne); // Indexation par NSS
            }
        }
    }

    /**
     * Loads administrative requests from `demandes.csv` and stores them in memory.
     */
    private void chargerDemandesDepuisCSV() throws IOException {
        InitialisationCSV loader = new InitialisationCSV();
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("demandes.csv")) {
            if (inputStream == null) {
                throw new IOException("Fichier utilisateurs.csv introuvable dans les ressources.");
            }
            Set<DemandeAdmin> demandes = loader.chargerDemandes(inputStream);
            for (DemandeAdmin demande : demandes) {
                demandesAdmins.add(demande);
                DemandeAdminService.ajouterDemande(demande);
            }
        }
    }

    /**
     * Builds genealogy trees by grouping users by family and finding the oldest known ancestor.
     */
    private void chargerArbres() {
        Map<String, ArbreGenealogique> arbresParFamille = new HashMap<>();

        for (Personne utilisateur : utilisateurs.values()) {
            if (utilisateur.getArbre() != null) continue;

            // Déterminer le racine/ancêtre pour cette personne
            Personne racine = trouverAncetre(utilisateur);
            String cleFamille = racine.getNss(); // Utilisation du NSS de l'ancêtre comme clé

            // Si aucun arbre n'existe pour cette racine, créer un nouvel arbre
            arbresParFamille.putIfAbsent(cleFamille, new ArbreGenealogique(racine));

            // Associer l'utilisateur à l'arbre de la famille
            ArbreGenealogique arbreFamille = arbresParFamille.get(cleFamille);
            utilisateur.setArbre(arbreFamille);

            // Ajouter l'utilisateur dans les nœuds de l'arbre
            arbreFamille.getNoeuds().add(utilisateur);
        }
    }

    /**
     * Traverses ancestry to find the root ancestor of a person.
     */
    private Personne trouverAncetre(Personne p) {
        Set<Personne> visites = new HashSet<>();
        while ((p.getPere() != null || p.getMere() != null) && !visites.contains(p)) {
            visites.add(p);
            if (p.getPere() != null) p = p.getPere();
            else if (p.getMere() != null) p = p.getMere();
        }
        return p;
    }

    /**
     * Checks if an account already exists using its email.
     *
     * @param email user email
     * @return true if it exists, false otherwise
     */
    public boolean existe(String email) {
        for (Personne utilisateur : utilisateurs.values()) {
            Compte compte = utilisateur.getCompte();
            if (compte.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Authenticates a user using login, email, or private code, and their password.
     *
     * @param identifiant login, email, or private code
     * @param motDePasse  password
     * @return the authenticated user if valid, otherwise null
     */
    public Personne authentifier(String identifiant, String motDePasse) {
        for (Personne utilisateur : utilisateurs.values()) {
            Compte compte = utilisateur.getCompte();
            if ((compte.getEmail().equals(identifiant) || compte.getLogin().equals(identifiant) || utilisateur.getCodePrive().equals(identifiant))
                    && compte.getMotDePasse().equals(motDePasse)) {
                return utilisateur;
            }
        }
        return null;
    }

    /**
     * Adds a registered user to the system and writes their info to the CSV file.
     *
     * @param personne the user to add
     */
    public void ajouterUtilisateur(Personne personne) {
        utilisateurs.put(personne.getNss(), personne);
        try {
            sauvegarderNouvelUtilisateur(personne);

            if (personne.getArbre() != null) {
                arbres.add(personne.getArbre());
                GlobalTreesManager.ajouterArbre(personne.getArbre());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Projet_Généalogique/ProjetGénéalogique/ressources/compteur.txt"))) {
            writer.write(String.valueOf(Compte.getCompteur()));
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du compteur.");
        }
    }

    /**
     * Adds an administrative request to the system and updates the request file.
     *
     * @param demande the request to add
     */
    public void ajouterDemande(DemandeAdmin demande){
        demandesAdmins.add(demande);
        DemandeAdminService.ajouterDemande(demande);
        try {
            sauvegarderDemande(demande);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Projet_Généalogique/ProjetGénéalogique/ressources/compteurD.txt"))) {
            writer.write(String.valueOf(DemandeAdmin.getCompteur()));
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du compteur.");
        }
    }

    /**
     * Saves only the newly added user to `utilisateurs.csv`.
     *
     * @param personne user to save
     * @throws IOException if write fails
     */
    public void sauvegarderNouvelUtilisateur(Personne personne) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);

        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            if (fichierVide) {
                writer.write("nss,prenom,nom,dateNaissance,nationalite,carteIdentite,email,telephone,adresse,codePrive,nssPere,nssMere,genre,login,motDePasse,numero,premiereConnexion,familleId,photo,inscrit");
                writer.newLine();
            }
            String ligne;

            if (!personne.isEstInscrit()) {
                ligne = String.join(",",
                        personne.getNss(),
                        personne.getPrenom(),
                        personne.getNom(),
                        personne.getDateNaissance().toString(),
                        personne.getNationalite(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        personne.getMere() != null ? personne.getMere().getNss() : "",
                        personne.getPere() != null ? personne.getPere().getNss() : "",
                        personne.getGenre().toString(),
                        "",
                        "",
                        "",
                        "",
                        personne.getFamilleId(),
                        "",
                        "false",
                        "false");
            } else {
                Compte c = personne.getCompte();
                ligne = String.join(",",
                        personne.getNss(),
                        personne.getPrenom(),
                        personne.getNom(),
                        personne.getDateNaissance().toString(),
                        personne.getNationalite(),
                        personne.getCarteIdentite(),
                        c.getEmail(),
                        c.getTelephone(),
                        c.getAdresse(),
                        personne.getCodePrive(),
                        personne.getPere() != null ? personne.getPere().getNss() : "",
                        personne.getMere() != null ? personne.getMere().getNss() : "",
                        personne.getGenre().toString(),
                        c.getLogin(),
                        c.getMotDePasse(),
                        c.getNumero(),
                        c.isPremiereConnexion() ? "true" : "false",
                        personne.getFamilleId(),
                        personne.getUrlPhoto(),
                        personne.isEstInscrit() ? "true" : "false",
                        personne.isValideParAdmin() ? "true" : "false",
                        personne.getNiveauVisibilite().toString());
            }
            writer.write(ligne);
            writer.newLine();
        }

    }

    /**
     * Saves a new administrative request to `demandes.csv`.
     *
     * @param demande request to save
     * @throws IOException if write fails
     */
    public void sauvegarderDemande(DemandeAdmin demande) throws IOException {
        Path path = Paths.get(DEMANDE_FILE_PATH);

        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            if (fichierVide) {
                writer.write("id,type,demandeurNSS,cibleNSS,dateCreation,lien,statut");
                writer.newLine();
            }
            String line = String.join(",",
                    demande.getId(),
                    demande.getType().toString(),
                    demande.getDemandeur().getNss(),
                    demande.getCible().getNss(),
                    demande.getDateCreation().toString(),
                    demande.getLien().toString() != null ? demande.getLien().toString() : "",
                    demande.getStatut().toString());
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Updates a user's information in `utilisateurs.csv`.
     *
     * @param personne the updated user
     */
    public void mettreAJourUtilisateur(Personne personne) {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);
        List<String> lignes;

        try {
            lignes = Files.readAllLines(path);
            String nss = personne.getNss();

            for (int i = 1; i < lignes.size(); i++) {
                String[] champs = lignes.get(i).split(",");
                if (champs.length > 20 && champs[0].equals(nss)) {
                    Compte c = personne.getCompte();
                    String nouvelleLigne = String.join(",",
                            personne.getNss(),
                            personne.getPrenom(),
                            personne.getNom(),
                            personne.getDateNaissance().toString(),
                            personne.getNationalite(),
                            personne.getCarteIdentite(),
                            c.getEmail(),
                            c.getTelephone(),
                            c.getAdresse(),
                            personne.getCodePrive(),
                            personne.getPere() != null ? personne.getPere().getNss() : "",
                            personne.getMere() != null ? personne.getMere().getNss() : "",
                            personne.getGenre().toString(),
                            c.getLogin(),
                            c.getMotDePasse(),
                            c.getNumero(),
                            c.isPremiereConnexion() ? "true" : "false",
                            personne.getFamilleId(),
                            personne.getUrlPhoto(),
                            personne.isEstInscrit() ? "true" : "false",
                            personne.isValideParAdmin() ? "true" : "false",
                            personne.getNiveauVisibilite().toString());// NiveauVisibilite

                    lignes.set(i, nouvelleLigne);
                    break;
                }
            }

            Files.write(path, lignes);
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    /**
     * Updates the status of an admin request in `demandes.csv`.
     *
     * @param demande the updated request
     */
    public void mettreAJourDemande(DemandeAdmin demande) {
        Path path = Paths.get(DEMANDE_FILE_PATH);
        List<String> lignes;
        try {
            lignes = Files.readAllLines(path);
            String id = demande.getId();

            for (int i = 1; i < lignes.size(); i++) {
                String[] champs = lignes.get(i).split(",");
                if (champs.length > 6 && champs[0].equals(id)) {
                    String nouvelleLigne = String.join(",",
                            demande.getId(),
                            demande.getType().toString(),
                            demande.getDemandeur().getNss(),
                            demande.getCible().getNss(),
                            demande.getDateCreation().toString(),
                            demande.getLien().toString(),
                            demande.getStatut().toString()
                    );

                    lignes.set(i, nouvelleLigne);
                    break;
                }
            }
            Files.write(path, lignes);
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour de la demande : " + e.getMessage());
        }
    }

    /**
     * Updates the nationality and gender of an unregistered person by name.
     *
     * @param nomCible       target last name
     * @param prenomCible    target first name
     * @param nouvelleNat    new nationality
     * @param nouveauGenre   new gender
     * @throws IOException if the file update fails
     */
    public static void modifierPersonne(String nomCible, String prenomCible, String nouvelleNat, Genre nouveauGenre) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);
        List<String> lignes = Files.readAllLines(path);
        for (int i = 1; i < lignes.size(); i++) {  // i=1 pour sauter l'en-tête
            String[] champs = lignes.get(i).split(",");
            if (champs.length >= 18 && champs[1].equals(prenomCible) && champs[2].equals(nomCible)) {
                // Mise à jour des champs Nationalité (index 4) et Genre (index 12)
                champs[4]  = nouvelleNat;
                champs[12] = nouveauGenre.toString();
                lignes.set(i, String.join(",", champs));
                break;
            }
        }
        Files.write(path, lignes);
    }

    /**
     * Finds a user by their social security number (NSS).
     *
     * @param nss the NSS to search
     * @return the matched user, or null if not found
     */
    public Personne getPersonneParNSS(String nss) {
        if (utilisateurs.containsKey(nss)) {
            System.out.println("✅ Personne trouvée pour NSS : " + nss); // DEBUG
            return utilisateurs.get(nss);
        } else {
            System.out.println("❌ Personne introuvable pour NSS : " + nss); // DEBUG
            return null;
        }
    }

    /**
     * Deletes a user from the CSV file using their NSS.
     *
     * @param nss the NSS of the user to delete
     */
    public void supprimerUtilisateurParNSS(String nss) {
        File inputFile = new File("ressources/utilisateurs.csv");
        File tempFile = new File("ressources/temp_utilisateurs.csv");

        boolean supprime = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String ligne;
            while ((ligne = reader.readLine()) != null) {
                String[] fields = ligne.split(",");
                if (fields.length == 0) continue;

                String nssCourant = fields[0];

                if (nssCourant.equals(nss)) {
                    supprime = true; // ligne ignorée
                    continue;
                }

                writer.write(ligne);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Remplacer l'ancien fichier par le nouveau
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("❌ Erreur lors de la mise à jour du fichier.");
        }

    }

    public boolean supprimerDemandeParID(String id) {
        File inputFile = new File("ressources/demandes.csv");
        File tempFile = new File("ressources/temp_demandes.csv");

        boolean supprime = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String ligne;
            while ((ligne = reader.readLine()) != null) {
                String[] fields = ligne.split(",");
                if (fields.length == 0) continue;

                String idCourant = fields[0];

                if (idCourant.equals(id)) {
                    supprime = true; // ligne ignorée
                    continue;
                }

                writer.write(ligne);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Remplacer l'ancien fichier par le nouveau
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("❌ Erreur lors de la mise à jour du fichier.");
            return false;
        }

        return supprime;
    }


}