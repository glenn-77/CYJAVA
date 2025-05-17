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
import service.MailService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;



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

        Label carteLabel = new Label("Carte d'identité :");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Button choisirImageBtn = new Button("Choisir une image");
        final File[] imageFile = new File[1];

        choisirImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imageFile[0] = selectedFile;
                imageView.setImage(new Image(selectedFile.toURI().toString()));
            }
        });

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
                String carteId = imageFile[0] != null ? imageFile[0].toURI().toString() : null;
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
                final String codePrive = UUID.randomUUID().toString().substring(0, 8);
                Compte compte = new Compte(prenom.toLowerCase() + "." + nom.toLowerCase().charAt(0), motDePasse, email, telephone, adresse);
                Personne p = new Personne(nss, prenom, nom, dateNaissance, nationalite,
                        carteId, codePrive, genre, compte, null);

                if (authService.existe(email)) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Cet email est déjà utilisé.");
                } else {
                    authService.ajouterUtilisateur(p);
                    p.setEstInscrit(true);
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Inscription réussie ! Code privé envoyé par email : " + codePrive + "\nRedirection vers la page de connexion...");
                    MailService.envoyerEmail(email,
                            "Bienvenue sur Arbre Généalogique Pro++",
                            "Bonjour " + prenom + ",\n\nVotre inscription est confirmée.\n\nVoici votre code privé : " + codePrive +
                                    "\n\nVotre mot de passe temporaire est votre prénom. Merci de le modifier lors de votre première connexion.");


                    nssField.clear(); nomField.clear(); prenomField.clear(); dateNaissancePicker.setValue(null);
                    nationaliteField.clear(); genreBox.setValue(null);
                    emailField.clear(); telephoneField.clear(); adresseField.clear();
                    PauseTransition pause = new PauseTransition(Duration.seconds(4));
                    pause.setOnFinished(ev -> {
                        LoginView loginView = new LoginView(authService);
                        loginView.start(stage);
                    });
                    pause.play();
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
                nationaliteField, carteLabel, choisirImageBtn, imageView, genreBox,
                emailField, telephoneField, adresseField,
                inscrireBtn, retourBtn, message
        );

        Scene scene = new Scene(layout, 800, 1200);
        stage.setScene(scene);
        stage.show();
    }
}
