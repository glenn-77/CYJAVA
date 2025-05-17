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
                utilisateurs.put(personne.getCompte().getEmail(), personne);
            }
        }
    }

    /**
     * Initialise les arbres généalogiques en fonction des utilisateurs.
     */
    private void chargerArbres() {
        for (Personne utilisateur : utilisateurs.values()) {
            if (utilisateur.getArbre() == null) {
                // Si aucun arbre n'est associé, on en crée un pour l'utilisateur
                ArbreGenealogique nouvelArbre = new ArbreGenealogique(utilisateur);
                utilisateur.setArbre(nouvelArbre);
                arbres.add(nouvelArbre);
                GlobalTreesManager.ajouterArbre(nouvelArbre);
            } else {
                // Ajouter l'arbre existant à la liste globale
                arbres.add(utilisateur.getArbre());
                GlobalTreesManager.ajouterArbre(utilisateur.getArbre());
            }
        }
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
     * @param email       L'email de l'utilisateur.
     * @param motDePasse  Le mot de passe de l'utilisateur.
     * @return L'objet Personne correspondant, ou null si l'authentification échoue.
     */
    public Personne authentifier(String email, String motDePasse) {
        if (utilisateurs.containsKey(email)) {
            Personne utilisateur = utilisateurs.get(email);
            if (utilisateur.getCompte().getMotDePasse().equals(motDePasse)) {
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
                    "");
            writer.write(ligne);
            writer.newLine();
        }
    }
}