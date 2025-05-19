package view;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.AuthService;
import dao.UserDAO;
import entites.Personne;
import java.util.Set;

public class RechercheView {

    public void start(Stage stage) {

        // 🔍 Barre de recherche + filtre
        TextField champRecherche = new TextField();
        champRecherche.setPromptText("Entrez un nom, prénom ou NSS");

        ComboBox<String> filtre = new ComboBox<>();
        filtre.getItems().addAll("Nom", "Prénom", "NSS");
        filtre.setValue("Nom");

        Button boutonRechercher = new Button("Rechercher");
        ListView<String> listeResultats = new ListView<>();

        boutonRechercher.setOnAction(e -> {
            String critere = filtre.getValue();
            String valeur = champRecherche.getText().trim();
            listeResultats.getItems().clear();

            if (valeur.isEmpty()) {
                listeResultats.getItems().add("⚠️ Champ vide.");
                return;
            }

            switch (critere) {
                case "Nom":
                    Set<Personne> parNom = UserDAO.chercherParNom(valeur);
                    if (parNom.isEmpty()) {
                        listeResultats.getItems().add("Aucun résultat pour ce nom.");
                    } else {
                        parNom.forEach(p -> listeResultats.getItems().add("Nom : " + p.getNom() + " | Prénom : " + p.getPrenom()));
                    }
                    break;
                case "Prénom":
                    Set<Personne> parPrenom = UserDAO.chercherParPrenom(valeur);
                    if (parPrenom.isEmpty()) {
                        listeResultats.getItems().add("Aucun résultat pour ce prénom.");
                    } else {
                        parPrenom.forEach(p -> listeResultats.getItems().add("Nom : " + p.getNom() + " | Prénom : " + p.getPrenom()));
                    }
                    break;
                case "NSS":
                    Personne p = UserDAO.chercherParNSS(valeur);
                    if (p != null) {
                        listeResultats.getItems().add("Nom : " + p.getNom() + " | Prénom : " + p.getPrenom());
                    } else {
                        listeResultats.getItems().add("Aucun résultat pour ce NSS.");
                    }
                    break;
            }
        });

        // 🔙 Bouton retour
        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> {
            MainView retourAccueil = new MainView(new AuthService());
            retourAccueil.start(stage);
        });

        VBox layout = new VBox(10, champRecherche, filtre, boutonRechercher, listeResultats, retourBtn);
        Scene scene = new Scene(layout, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Recherche unifiée");
    }
}
