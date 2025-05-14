package view;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Personne;
import initialisation.InitialisationCSV;


public class AuthService {

    private final Map<String, Personne> utilisateurs;

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
        // Utilisation du ClassLoader pour trouver le fichier dans les ressources
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
            if (p.getCompte().getMotDePasse().equals(motDePasse)) {  // Vérifie le mot de passe via le compte
                return p;
            }
        }
        return null;
    }
}
