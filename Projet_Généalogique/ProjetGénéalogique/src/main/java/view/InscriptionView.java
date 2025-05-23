package view;

import entites.*;
import entites.enums.Genre;
import entites.enums.LienParente;
import entites.enums.TypeDemande;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import service.AuthService;
import service.MailService;
import service.DemandeAdminService.DemandeAdmin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import java.io.File;


/**
 * A JavaFX view that handles the user registration process.
 * Displays a form to collect personal information and sends a registration request
 * to the administrator after submission. If the form is valid, a private code and temporary
 * password are generated and emailed to the user.
 */
public class InscriptionView {

    private final AuthService authService;
    private boolean isDarkMode = false;

    /**
     * Constructs the InscriptionView with the given authentication service.
     *
     * @param authService service used to manage authentication and user registration
     */
    public InscriptionView(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Starts the JavaFX window for user registration.
     * Collects information including name, date of birth, nationality, gender, and contact details.
     * A photo of the ID is also required.
     *
     * @param stage the JavaFX stage where the scene is displayed
     */
    public void start(Stage stage) {
        stage.setTitle("Inscription");

        VBox root = new VBox();
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label titleLabel = new Label("Inscrivez-vous !");
        titleLabel.setId("title");

        // Input fields
        TextField nssField = new TextField();
        nssField.setPromptText("Numéro de Sécurité Sociale");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        DatePicker dateNaissancePicker = new DatePicker();
        dateNaissancePicker.setPromptText("Date de naissance (jj/MM/aaaa)");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateNaissancePicker.setConverter(new StringConverter<>() {
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
        inscrireBtn.setId("inscrire-button");

        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> new MainView(authService).start(stage));

        Label message = new Label();
        message.setId("message-label");

        Button themeButton = new Button("Mode sombre");
        themeButton.setOnAction(e -> {
            if (isDarkMode) {
                root.getStyleClass().remove("dark-mode");
                themeButton.setText("Mode sombre");
                isDarkMode = false;
            } else {
                root.getStyleClass().add("dark-mode");
                themeButton.setText("Mode clair");
                isDarkMode = true;
            }
        });

        inscrireBtn.setOnAction(e -> {
            try {
                // Collect input values
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

                // Check for empty fields
                if (nss.isEmpty() || nom.isEmpty() || prenom.isEmpty() || dateNaissance == null ||
                        nationalite.isEmpty() || carteId == null || genre == null ||
                        email.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
                    message.setText("Merci de remplir tous les champs.");
                    return;
                }

                // Generate credentials
                final String codePrive = UUID.randomUUID().toString().substring(0, 8);
                Compte compte = new Compte(prenom.toLowerCase() + "." + nom.toLowerCase().charAt(0), prenom, email, telephone, adresse);
                Personne p = new Personne(nss, prenom, nom, dateNaissance, nationalite,
                        carteId, codePrive, genre, compte, null);

                // Check for existing email
                if (authService.existe(email)) {
                    message.setStyle("-fx-text-fill: red;");
                    message.setText("Cet email est déjà utilisé.");
                } else {
                    p.setEstInscrit(true);
                    authService.ajouterUtilisateur(p);
                    authService.ajouterDemande(new DemandeAdmin(p, p, LienParente.INSCRIPTION, TypeDemande.AJOUT_PERSONNE));
                    message.setStyle("-fx-text-fill: green;");
                    message.setText("Demande d'inscription envoyée à l'admin. Code privé envoyé par email : " + codePrive + "\nRedirection vers la page de connexion...");
                    MailService.envoyerEmail(email,
                            "Bienvenue sur Arbre Généalogique Pro++",
                            "Bonjour " + prenom + ",\n\nVotre inscription est en attente de confirmation .\n\nVoici votre code privé : " + codePrive +
                                    "\n\nVotre mot de passe temporaire est votre prénom. Merci de le modifier lors de votre première connexion lorsque votre inscription sera confirmée.");


                    // Clear fields
                    nssField.clear(); nomField.clear(); prenomField.clear(); dateNaissancePicker.setValue(null);
                    nationaliteField.clear(); genreBox.setValue(null);
                    emailField.clear(); telephoneField.clear(); adresseField.clear();
                    PauseTransition pause = new PauseTransition(Duration.seconds(4));
                    pause.setOnFinished(ev -> new LoginView(authService).start(stage));
                    pause.play();
                }
            } catch (Exception ex) {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Erreur : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox formBox = new VBox(15,
                titleLabel,
                nssField, nomField, prenomField,
                dateNaissancePicker, nationaliteField,
                genreBox, emailField, telephoneField, adresseField,
                carteLabel, choisirImageBtn, imageView,
                inscrireBtn, retourBtn, themeButton,
                message
        );
        formBox.setAlignment(Pos.CENTER);
        formBox.getStyleClass().add("form-container");

        root.getChildren().add(formBox);

        Scene scene = new Scene(root, 850, 1100);
        scene.getStylesheets().add(getClass().getResource("/inscription.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
