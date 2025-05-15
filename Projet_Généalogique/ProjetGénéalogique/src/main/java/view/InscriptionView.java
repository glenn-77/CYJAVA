package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.*;
import service.AuthService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class InscriptionView {

    private final AuthService authService;

    public InscriptionView(AuthService authService) {
        this.authService = authService;
    }

    public void start(Stage stage) {
        stage.setTitle("Inscription");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Label titleLabel = new Label("Inscrivez-vous !");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nssField = new TextField();
        nssField.setPromptText("Numéro de Sécurité Sociale");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        DatePicker dateNaissancePicker = new DatePicker();
        dateNaissancePicker.setPromptText("Date de naissance (jj/MM/aaaa)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        dateNaissancePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    return LocalDate.parse(string, formatter);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });

        TextField nationaliteField = new TextField();
        nationaliteField.setPromptText("Nationalité");

        TextField carteIdentiteField = new TextField();
        carteIdentiteField.setPromptText("Carte d'identité");

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
        inscrireBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> {
            MainView mainView = new MainView(authService);
            mainView.start(stage);
        });

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

                if (nss.isEmpty() || nom.isEmpty() || prenom.isEmpty() || dateNaissance == null ||
                        nationalite.isEmpty() || carteId.isEmpty() || genre == null ||
                        email.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Merci de remplir tous les champs.");
                    return;
                }

                String motDePasse = prenom;
                String codePrive = UUID.randomUUID().toString().substring(0, 8);
                Compte compte = new Compte(prenom, motDePasse, nss, email, telephone, adresse);
                Personne p = new Personne(nss, prenom, nom, dateNaissance, nationalite,
                        carteId, codePrive, genre, compte, null);

                if (authService.existe(email)) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Cet email est déjà utilisé.");
                } else {
                    authService.ajouterUtilisateur(p);
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Inscription réussie ! Code privé envoyé par email : " + codePrive);

                    nssField.clear(); nomField.clear(); prenomField.clear(); dateNaissancePicker.setValue(null);
                    nationaliteField.clear(); carteIdentiteField.clear(); genreBox.setValue(null);
                    emailField.clear(); telephoneField.clear(); adresseField.clear();
                }
            } catch (Exception ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Erreur : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(
                titleLabel,
                nssField, nomField, prenomField, dateNaissancePicker,
                nationaliteField, carteIdentiteField, genreBox,
                emailField, telephoneField, adresseField,
                inscrireBtn, retourBtn, message
        );

        Scene scene = new Scene(layout, 400, 650);
        stage.setScene(scene);
        stage.show();
    }
}
