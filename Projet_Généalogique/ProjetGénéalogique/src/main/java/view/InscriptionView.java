package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class InscriptionView {
    private AuthService authService;

    public InscriptionView(AuthService authService) {
        this.authService = authService;
    }

    public void start(Stage stage) {
        stage.setTitle("Inscription");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField nssField = new TextField();
        nssField.setPromptText("Numéro de Sécurité Sociale");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        DatePicker dateNaissancePicker = new DatePicker();
        dateNaissancePicker.setPromptText("Date de naissance (jj/MM/aaaa)");

        // Formatter pour le format "dd/MM/yyyy"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Configuration du converter pour afficher et parser la date au bon format
        dateNaissancePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string, formatter);
                } catch (DateTimeParseException e) {
                    // En cas d’erreur de parsing, on peut afficher un message ou retourner null
                    return null;
                }
            }
        });

        TextField nationaliteField = new TextField();
        nationaliteField.setPromptText("Nationalité");

        TextField carteIdentiteField = new TextField();
        carteIdentiteField.setPromptText("Carte d'identité (nom du fichier)");

        ComboBox<Genre> genreBox = new ComboBox<>();
        genreBox.getItems().addAll(Genre.HOMME, Genre.FEMME);
        genreBox.setPromptText("Genre");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");

        TextField adresseField = new TextField();
        adresseField.setPromptText("Adresse");

        Button inscrireBtn = new Button("S'inscrire");
        Label message = new Label();

        inscrireBtn.setOnAction(e -> {
            try {
                String nss = nssField.getText();
                String nom = nomField.getText();
                String prenom = prenomField.getText();
                LocalDate dateNaissance = dateNaissancePicker.getValue();
                String nationalite = nationaliteField.getText();
                String carteId = carteIdentiteField.getText();
                Genre genre = genreBox.getValue();
                String email = emailField.getText();
                String telephone = telephoneField.getText();
                String adresse = adresseField.getText();

                // Vérifications basiques
                if (nss.isEmpty() || nom.isEmpty() || prenom.isEmpty() || dateNaissance == null || nationalite.isEmpty()
                        || carteId.isEmpty() || genre == null || email.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Merci de remplir tous les champs.");
                    return;
                }

                // Mot de passe initial = prénom
                String motDePasse = prenom;

                // Génération code privé
                String codePrive = UUID.randomUUID().toString().substring(0, 8);

                // Création du compte
                Compte compte = new Compte(prenom, motDePasse, nss, email, telephone, adresse);

                // Création de la personne
                Personne p = new Personne(nss, prenom, nom, dateNaissance, nationalite,
                        carteId, codePrive, genre, compte, null);

                // Ajout dans le service d'authentification
                if (authService.existe(email)) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Cet email est déjà utilisé.");
                } else {
                    authService.ajouterUtilisateur(p);

                    // Simulation d'envoi de mail
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Inscription réussie ! Code privé envoyé par email : " + codePrive);

                    // Optionnel : reset des champs après inscription réussie
                    nssField.clear();
                    nomField.clear();
                    prenomField.clear();
                    dateNaissancePicker.setValue(null);
                    nationaliteField.clear();
                    carteIdentiteField.clear();
                    genreBox.setValue(null);
                    emailField.clear();
                    telephoneField.clear();
                    adresseField.clear();
                }

            } catch (Exception ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Erreur : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(
                nssField, nomField, prenomField, dateNaissancePicker,
                nationaliteField, carteIdentiteField, genreBox,
                emailField, telephoneField, adresseField,
                inscrireBtn, message
        );

        Scene scene = new Scene(layout, 400, 600);
        stage.setScene(scene);
        stage.show();
    }
}
