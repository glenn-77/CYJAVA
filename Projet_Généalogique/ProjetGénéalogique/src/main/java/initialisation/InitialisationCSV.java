package initialisation;

import model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InitialisationCSV {

    public List<Personne> chargerUtilisateurs(String filePath) throws IOException {
        List<Personne> utilisateurs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Ignorer l'en-tête

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                if (values.length < 14) {
                    System.err.println("Ligne mal formatée, ignorée : " + line);
                    continue;
                }

                try {
                    // Informations de la personne
                    String nss = values[0].trim();
                    String prenom = values[1].trim();
                    String nom = values[2].trim();
                    LocalDate dateNaissance = LocalDate.parse(values[3].trim());
                    String nationalite = values[4].trim();
                    String carteIdentite = values[5].trim();
                    String codePrive = values[9].trim();
                    Genre genre = Genre.valueOf(values[10].trim().toUpperCase());

                    // Informations du compte
                    String email = values[6].trim();
                    String telephone = values[7].trim();
                    String adresse = values[8].trim();
                    String login = values[11].trim();
                    String motDePasse = values[12].trim();
                    String numero = values[13].trim();

                    // Création de l'objet Compte
                    Compte compte = new Compte(login, motDePasse, numero, email, telephone, adresse);

                    // Création de l'objet Personne avec le compte
                    Personne personne = new Personne(nss, prenom, nom, dateNaissance, nationalite, carteIdentite, 
                                                      codePrive, genre, compte, null );

                    utilisateurs.add(personne);

                } catch (Exception e) {
                    System.err.println("Erreur de traitement de la ligne : " + line + " -> " + e.getMessage());
                }
            }
        }

        return utilisateurs;
    }
}
