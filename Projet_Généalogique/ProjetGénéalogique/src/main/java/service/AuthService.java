package service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import model.ArbreGenealogique;
import model.Compte;
import model.Personne;
import initialisation.InitialisationCSV;

public class AuthService {

    private final Map<String, Personne> utilisateurs;
    private final List<ArbreGenealogique> arbres; // Liste des arbres généalogiques

    // Chemin vers le fichier CSV
    private static final String UTILISATEURS_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";

    public AuthService() {
        this.utilisateurs = new HashMap<>();
        this.arbres = new ArrayList<>(); // Initialisation de la liste des arbres
        try {
            chargerUtilisateursDepuisCSV();
            chargerArbres(); // Initialiser les arbres généalogiques
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
                utilisateurs.put(personne.getCompte().getEmail(), personne);
            }
        }
    }

    /**
     * Initialise les arbres généalogiques en fonction des utilisateurs.
     */
    private void chargerArbres() {
        for (Personne utilisateur : utilisateurs.values()) {
            ArbreGenealogique arbre = utilisateur.getArbre();
            if (arbre != null && !arbres.contains(arbre)) {
                arbres.add(arbre);
            }
        }
    }

    /**
     * Authentifie un utilisateur via son email et mot de passe.
     *
     * @param email       L'email de l'utilisateur.
     * @param motDePasse  Le mot de passe de l'utilisateur.
     * @return La personne correspondante si la connexion est réussie, sinon null.
     */
    public Personne authentifier(String email, String motDePasse) {
        if (utilisateurs.containsKey(email)) {
            Personne p = utilisateurs.get(email);
            if (p.getCompte().getMotDePasse().equals(motDePasse)) {
                return p;
            }
        }
        return null;
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne la liste de tous les arbres généalogiques disponibles.
     *
     * @return Liste des arbres généalogiques.
     */
    public List<ArbreGenealogique> getArbres() {
        return arbres; // Fournit la liste des arbres au besoin.
    }

    /**
     * Sauvegarde uniquement un nouvel utilisateur dans le fichier, sans réécrire tout le contenu.
     *
     * @param personne La personne à enregistrer.
     * @throws IOException Si une erreur d'écriture se produit.
     */
    public void sauvegarderNouvelUtilisateur(Personne personne) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);

        // Création des dossiers si besoin
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            // Écrire l'en-tête uniquement si le fichier est vide
            if (fichierVide) {
                writer.write("nss,prenom,nom,dateNaissance,nationalite,carteIdentite,email,telephone,adresse,codePrive,genre,login,motDePasse,numero");
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
                    personne.getGenre().toString(),
                    c.getLogin(),
                    c.getMotDePasse(),
                    ""); // champ "numero" vide

            writer.write(ligne);
            writer.newLine();
        }
    }
}