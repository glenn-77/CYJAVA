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
            chargerUtilisateursDepuisCSV("ressources/utilisateurs.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerUtilisateursDepuisCSV(String filePath) throws IOException {
        InitialisationCSV loader = new InitialisationCSV(); 
        List<Personne> utilisateursList = loader.chargerUtilisateurs(filePath);
        for (Personne personne : utilisateursList) {
            utilisateurs.put(personne.getCompte().getEmail(), personne); // Utilise l'email du compte
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
