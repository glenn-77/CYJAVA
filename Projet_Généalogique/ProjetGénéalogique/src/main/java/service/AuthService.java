package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import entites.ArbreGenealogique;
import entites.Compte;
import entites.Personne;
import initialisation.InitialisationCSV;
import java.time.LocalDate;
import entites.Genre;

/**
 * Service d'authentification et gestion des utilisateurs et arbres généalogiques.
 */
public class AuthService {

    private final Map<String, Personne> utilisateurs; // Email -> Personne
    private final List<ArbreGenealogique> arbres; // Liste des arbres généalogiques

    private static final String UTILISATEURS_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";

    public AuthService() {
        this.utilisateurs = new HashMap<>();
        this.arbres = new ArrayList<>();
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
            chargerArbres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge les utilisateurs depuis un fichier CSV.
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
     * Initialise les arbres généalogiques en fonction des utilisateurs.
     */
    private void chargerArbres() {
        Map<String, ArbreGenealogique> arbresParFamille = new HashMap<>();

        for (Personne utilisateur : utilisateurs.values()) {
            if (utilisateur.getArbre() != null) continue;

            Personne racine = trouverAncetre(utilisateur);
            String cleFamille = racine.getNss();

            if (!arbresParFamille.containsKey(cleFamille)) {
                ArbreGenealogique arbre = new ArbreGenealogique(racine);
                arbresParFamille.put(cleFamille, arbre);
                GlobalTreesManager.ajouterArbre(arbre);
                arbres.add(arbre);
            }

            ArbreGenealogique arbreFamille = arbresParFamille.get(cleFamille);
            utilisateur.setArbre(arbreFamille);
        }
    }

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
     * Vérifie si un utilisateur existe via son email.
     *
     * @param email L'email de l'utilisateur.
     * @return true s'il existe, sinon false.
     */
    public boolean existe(String email) {
        return utilisateurs.containsKey(email);
    }

    /**
     * Authentifie un utilisateur via son email et mot de passe.
     *
     * @param identifiant       L'email ou login de l'utilisateur.
     * @param motDePasse  Le mot de passe de l'utilisateur.
     * @return L'objet Personne correspondant, ou null si l'authentification échoue.
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
     * Récupère la liste des arbres généalogiques.
     *
     * @return Liste des arbres généalogiques disponibles.
     */
    public List<ArbreGenealogique> getArbres() {
        return arbres;
    }

    /**
     * Ajoute un utilisateur et met à jour le fichier CSV.
     *
     * @param personne La personne à ajouter.
     */
    public void ajouterUtilisateur(Personne personne) {
        utilisateurs.put(personne.getCompte().getEmail(), personne);
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
     * Sauvegarde uniquement un nouvel utilisateur dans le fichier CSV.
     *
     * @param personne La personne à enregistrer.
     * @throws IOException Si une erreur d'écriture se produit.
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
                writer.write("nss,prenom,nom,dateNaissance,nationalite,carteIdentite,email,telephone,adresse,codePrive,nssPere,nssMere,genre,login,motDePasse,numero,premiereConnexion,familleId,photo");
                writer.newLine();
            }

            Compte c = personne.getCompte();
            String ligne = String.join(",",
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
                    personne.getFamilleId() != null ? personne.getFamilleId() : "",
                    personne.getUrlPhoto() != null ? personne.getUrlPhoto() : "");
            writer.write(ligne);
            writer.newLine();
        }

    }

    public void mettreAJourUtilisateur(Personne personne) {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);
        List<String> lignes = new ArrayList<>();

        try {
            lignes = Files.readAllLines(path);
            String nss = personne.getNss();

            for (int i = 1; i < lignes.size(); i++) {
                String[] champs = lignes.get(i).split(",");
                if (champs.length > 12 && champs[0].equals(nss)) {
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
                            personne.getFamilleId() != null ? personne.getFamilleId() : "",
                            personne.getUrlPhoto() != null ? personne.getUrlPhoto() : "");

                    lignes.set(i, nouvelleLigne);
                    break;
                }
            }

            Files.write(path, lignes);
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    // Ajoute une nouvelle personne dans le CSV (ajout d'une ligne)
    public static void ajouterPersonne(String nom, String prenom, LocalDate dateNaissance,
                                       String nationalite, Genre genre) throws IOException {
        // Générer un NSS unique pour la nouvelle personne (par ex. incrémental ou UUID)
        String nouveauNss = "ID" + System.currentTimeMillis();  // Ex. simple: à améliorer selon besoins
        // Préparer la nouvelle ligne CSV avec 18 colonnes
        String dateStr = dateNaissance.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String[] champs = new String[18];
        champs[0]  = nouveauNss;
        champs[1]  = prenom;
        champs[2]  = nom;
        champs[3]  = dateStr;
        champs[4]  = nationalite;
        champs[5]  = "";  // carteIdentite non fournie
        champs[6]  = "";  // email non fourni
        champs[7]  = "";  // telephone non fourni
        champs[8]  = "";  // adresse non fournie
        champs[9]  = "";  // codePrive non fourni
        champs[10] = "";  // nssPere inconnu
        champs[11] = "";  // nssMere inconnu
        champs[12] = genre.toString();
        champs[13] = "";  // login non applicable
        champs[14] = "";  // motDePasse non applicable
        champs[15] = "";  // numéro (compteur) non applicable
        champs[16] = "false";  // premiereConnexion par défaut
        champs[17] = "";  // familleId non déterminé
        champs[18] = "";
        String nouvelleLigne = String.join(",", champs);
        // Écriture en fin de fichier CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(UTILISATEURS_FILE_PATH, true))) {
            writer.newLine();
            writer.write(nouvelleLigne);
        }
    }

    // Modifie la nationalité et le genre d'une personne existante identifiée par son NSS
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

    public List<Personne> chargerToutesPersonnes() throws IOException {
        List<Personne> personnes = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(UTILISATEURS_FILE_PATH))) {
            String header = reader.readLine();  // sauter la ligne d'en-tête
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 18) continue;  // ligne invalide si pas assez de colonnes
                // Extraction des champs principaux d'après l'ordre du CSV
                String prenom     = values[1].trim();
                String nom        = values[2].trim();
                String dateStr    = values[3].trim();
                String nationalite= values[4].trim();
                String genreStr   = values[12].trim().toUpperCase();
                LocalDate dateNaissance;
                try {
                    dateNaissance = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    continue; // date invalide, on ignore cette entrée
                }
                Genre genre = Genre.valueOf(genreStr);
                personnes.add(new Personne(nom, prenom, dateNaissance, nationalite, genre));
            }
        }
        return personnes;
    }

    public Personne getPersonneParNSS(String nss) {
        if (utilisateurs.containsKey(nss)) {
            System.out.println("✅ Personne trouvée pour NSS : " + nss); // DEBUG
            return utilisateurs.get(nss);
        } else {
            System.out.println("❌ Personne introuvable pour NSS : " + nss); // DEBUG
            return null;
        }
    }



}