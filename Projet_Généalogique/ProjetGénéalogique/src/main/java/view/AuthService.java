package view;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Compte;
import model.Personne;
import initialisation.InitialisationCSV;

public class AuthService {

    private final Map<String, Personne> utilisateurs;

    // Chemin vers le fichier CSV
    private static final String UTILISATEURS_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";

    public AuthService() {
        this.utilisateurs = new HashMap<>();
        try {
            chargerUtilisateursDepuisCSV();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public Personne authentifier(String email, String motDePasse) {
        if (utilisateurs.containsKey(email)) {
            Personne p = utilisateurs.get(email);
            if (p.getCompte().getMotDePasse().equals(motDePasse)) {
                return p;
            }
        }
        return null;
    }

    public boolean existe(String email) {
        return utilisateurs.containsKey(email);
    }

    public void ajouterUtilisateur(Personne personne) {
        utilisateurs.put(personne.getCompte().getEmail(), personne);
        try {
            sauvegarderNouvelUtilisateur(personne);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sauvegarde uniquement le nouvel utilisateur dans le fichier (sans réécrire tout ni l'en-tête)
    public void sauvegarderNouvelUtilisateur(Personne personne) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);

        // Création des dossiers si besoin
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            // Écrire l'entête uniquement si le fichier est vide
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
