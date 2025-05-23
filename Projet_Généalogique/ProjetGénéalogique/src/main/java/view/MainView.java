package view;

import entites.Admin;
import entites.enums.LienParente;
import entites.enums.TypeDemande;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Group;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entites.enums.Genre;

import java.util.logging.Logger;
import java.util.logging.Level;

import service.AuthService;
import entites.Personne;
import service.MailService;
import service.DemandeAdminService.DemandeAdmin;
import entites.enums.NiveauVisibilite;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * JavaFX main dashboard for authenticated and non-authenticated users.
 * Displays profile information, navigation buttons, and tree-related features.
 * Allows access to genealogical tree visualization, statistics, photos, requests, and account management.
 */
public class MainView {

    private final AuthService authService;
    private final Personne utilisateur;
    private boolean isDarkMode = false;
    private final Logger LOGGER = Logger.getLogger(MainView.class.getName());

    /**
     * Constructs the main view for unauthenticated use.
     *
     * @param authService the authentication service
     */
    public MainView(AuthService authService) {
        this(authService, null);
    }

    /**
     * Constructs the main view for an authenticated user.
     *
     * @param authService the authentication service
     * @param utilisateur the connected user
     */
    public MainView(AuthService authService, Personne utilisateur) {
        this.authService = authService;
        this.utilisateur = utilisateur;
    }

    /**
     * Starts the view and displays the home screen.
     * Shows personalized content and features based on authentication state.
     *
     * @param stage the main JavaFX stage
     */
    public void start(Stage stage) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 1000, 1000);
        scene.getStylesheets().add(getClass().getResource("/mainview.css").toExternalForm());

        ToggleButton themeToggle = new ToggleButton("🌙 Mode sombre");
        themeToggle.getStyleClass().add("toggle-theme");
        themeToggle.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            if (isDarkMode) {
                layout.getStyleClass().add("dark-mode");
                themeToggle.setText("☀️ Mode clair");
            } else {
                layout.getStyleClass().remove("dark-mode");
                themeToggle.setText("🌙 Mode sombre");
            }
        });
        layout.getChildren().add(themeToggle);

        if (utilisateur != null) {
            Label bienvenue = new Label("Bienvenue " + utilisateur.getPrenom() + " !");
            bienvenue.getStyleClass().add("welcome-label");

            // 📸 Photo de profil
            ImageView imageView = new ImageView();
            imageView.setFitHeight(150);
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("profile-picture");

            try {
                File file = new File("Projet_Généalogique/ProjetGénéalogique/ressources/" + utilisateur.getUrlPhoto());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } else {
                    System.out.println("Fichier image non trouvé : " + file.getPath());
                    imageView.setImage(new Image(new File("Projet_Généalogique/ProjetGénéalogique/ressources/images/default.png").toURI().toString()));
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading profile image", e);
            }

            // 🖼 Modifier la photo
            Button modifierPhotoBtn = new Button("🖼 Modifier ma photo");
            Button voirMonArbreBtn = new Button("🌳 Voir mon arbre familial");
            Button voirTousArbresBtn = new Button("👥 Voir tous les arbres");
            Button souvenirsBtn = new Button("📸 Souvenirs");
            Button statistiquesButton = new Button("📊 Voir les statistiques de mon arbre");
            Button rechercheBtn = new Button("🔍 Rechercher une personne");
            Button modifierCompteButton = new Button("Modifier mon compte");
            Button logoutButton = new Button("🔴 Se déconnecter");

            List<Button> boutons = Arrays.asList(
                    modifierPhotoBtn, voirMonArbreBtn, voirTousArbresBtn,
                    souvenirsBtn, rechercheBtn, statistiquesButton,
                    modifierCompteButton
            );
            for (Button b : boutons) {
                b.getStyleClass().add("button");
            }

            logoutButton.getStyleClass().add("logout-button");

            modifierPhotoBtn.setOnAction(e -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Choisir une nouvelle photo");
                        fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                        );

                        File fichier = fileChooser.showOpenDialog(stage);
                        if (fichier != null) {
                            String nomFichier = fichier.getName();
                            String cheminRelatif = "images/" + nomFichier;

                            try {
                                File dossierImages = new File("Projet_Généalogique/ProjetGénéalogique/ressources/images/");
                                if (!dossierImages.exists()) {
                                    dossierImages.mkdirs(); // crée le dossier si nécessaire
                                }

                                File destination = new File("Projet_Généalogique/ProjetGénéalogique/ressources/" + cheminRelatif);
                                Files.copy(fichier.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("✅ Fichier copié dans : " + destination.getAbsolutePath());

                                // ✅ Met à jour l’objet
                                utilisateur.setUrlPhoto(cheminRelatif);

                                // ✅ Met à jour le CSV
                                authService.mettreAJourUtilisateur(utilisateur);

                                // ✅ Rafraîchit l'image affichée
                                Image nouvelleImage = new Image(new FileInputStream("Projet_Généalogique/ProjetGénéalogique/ressources/" + cheminRelatif));
                                imageView.setImage(nouvelleImage);

                            } catch (IOException ex) {
                                ex.printStackTrace();
                                new Alert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour de la photo.").show();
                            }
                        }
                    });

            statistiquesButton.setOnAction(e -> new AffichageConsultations(authService).afficherStatistiques(utilisateur.getNss()));
            rechercheBtn.setOnAction(e -> new RechercheView(authService, utilisateur).start(stage));
            souvenirsBtn.setOnAction(e -> new SouvenirsView(authService, utilisateur).start(stage));
            modifierCompteButton.setOnAction(e -> new ModifierCompteView(utilisateur, authService).start(stage));
            logoutButton.setOnAction(e -> new MainView(authService).start(stage));


            voirMonArbreBtn.setOnAction(e -> {
                BorderPane arbreView = new BorderPane();

                HBox topBar = new HBox();
                topBar.setAlignment(Pos.TOP_RIGHT);
                topBar.setPadding(new Insets(10));
                Button formButton = new Button("Ajouter/Modifier un nœud");
                formButton.getStyleClass().add("button");
                formButton.setOnAction(event -> ouvrirFormulaire(stage));
                topBar.getChildren().add(formButton);
                arbreView.setTop(topBar);
                formButton.setOnAction(event -> ouvrirFormulaire(stage));

                Group arbreGroup = new Group();
                ScrollPane scrollPane = new ScrollPane(arbreGroup);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                arbreView.setCenter(scrollPane);

                new AffichageArbre(utilisateur, stage).afficher(arbreGroup);

                Button retourBtn = new Button("🔙 Retour");
                retourBtn.setOnAction(event -> new MainView(authService, utilisateur).start(stage));
                arbreView.setBottom(retourBtn);
                BorderPane.setMargin(retourBtn, new Insets(10));

                Scene arbreScene = new Scene(arbreView, 1400, 1000);
                arbreScene.getStylesheets().add(getClass().getResource("/mainview.css").toExternalForm());
                stage.setScene(arbreScene);
            });

            voirTousArbresBtn.setOnAction(e -> new AllTreesView(utilisateur, stage).afficher());

            layout.getChildren().addAll(
                    bienvenue, imageView, modifierPhotoBtn, voirMonArbreBtn,
                    voirTousArbresBtn, souvenirsBtn, rechercheBtn, statistiquesButton
            );

            if (utilisateur.getCompte().getEmail().equalsIgnoreCase("diffoglenn007@gmail.com")) {
                Button voirDemandesBtn = new Button("📬 Voir les demandes");
                voirDemandesBtn.getStyleClass().add("button");
                voirDemandesBtn.setOnAction(e -> new DemandesAdminView(authService, utilisateur).start(stage));
                layout.getChildren().add(voirDemandesBtn);
            }
            layout.getChildren().addAll(modifierCompteButton, logoutButton);
        } else {
            Label titleLabel = new Label("Bienvenue !");
            titleLabel.getStyleClass().add("welcome-label");

            Button loginButton = new Button("Se connecter");
            Button registerButton = new Button("S'inscrire");
            loginButton.getStyleClass().add("button");
            registerButton.getStyleClass().add("button");

            loginButton.setOnAction(e -> new LoginView(authService).start(stage));
            registerButton.setOnAction(e -> new InscriptionView(authService).start(stage));

            layout.getChildren().addAll(titleLabel, loginButton, registerButton);
        }

        stage.setScene(scene);
        stage.setTitle("Accueil");
        stage.show();
    }
    /**
     * Opens a modal window to add or modify a person in the genealogical tree.
     * The form adapts based on the selected action ("Ajout" or "Modification").
     *
     * @param parentStage the parent stage for modal ownership
     */
    private void ouvrirFormulaire(Stage parentStage) {
        Stage formStage = new Stage();
        formStage.initOwner(parentStage);
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.setTitle("Ajout / Modification de personne");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label actionLabel = new Label("Action :");
        ChoiceBox<String> actionChoice = new ChoiceBox<>(
                FXCollections.observableArrayList("Ajout", "Modification"));
        actionChoice.setValue("Ajout");

        HBox actionRow = new HBox(10, actionLabel, actionChoice);

        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        DatePicker datePicker = new DatePicker();
        VBox addFieldsBox = new VBox(
                new HBox(5, new Label("Nom :"), nomField),
                new HBox(5, new Label("Prénom :"), prenomField),
                new HBox(5, new Label("Date de naissance :"), datePicker)
        );

        ComboBox<Personne> personCombo = new ComboBox<>();
        Set<Personne> proches = utilisateur.getLiens().keySet();
        personCombo.setItems(FXCollections.observableArrayList(proches));
        VBox modifyBox = new VBox(new HBox(5, new Label("Personne :"), personCombo));
        modifyBox.setVisible(false);
        modifyBox.setManaged(false);

        TextField natField = new TextField();
        ComboBox<Genre> genreCombo = new ComboBox<>(FXCollections.observableArrayList(Genre.values()));
        ComboBox<LienParente> lienCombo = new ComboBox<>(FXCollections.observableArrayList(LienParente.values()));
        VBox champsCommuns = new VBox(
                new HBox(5, new Label("Nationalité :"), natField),
                new HBox(5, new Label("Genre :"), genreCombo),
                new HBox(5, new Label("Lien :"), lienCombo)
        );

        actionChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean ajout = newVal.equals("Ajout");
            addFieldsBox.setVisible(ajout);
            addFieldsBox.setManaged(ajout);
            modifyBox.setVisible(!ajout);
            modifyBox.setManaged(!ajout);
        });

        Button envoyerBtn = new Button("Envoyer");
        envoyerBtn.setOnAction(event -> {
            try {
                if (actionChoice.getValue().equals("Ajout")) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Confirmer l’ajout de cette personne ?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            final String nss = UUID.randomUUID().toString().substring(0, 8);
                            Personne cible = new Personne(nss, prenomField.getText(), nomField.getText(), datePicker.getValue(), natField.getText(), null, null, genreCombo.getValue(), null, null);
                            LienParente lien = lienCombo.getValue();
                            if (utilisateur.getCompte() instanceof Admin) {
                                authService.ajouterUtilisateur(cible);
                                ((Admin) utilisateur.getCompte()).validerAjoutLien(utilisateur, cible, lien);
                                Alert info = new Alert(Alert.AlertType.INFORMATION, "Nouvelle personne ajoutée à l'arbre!");
                                info.show();
                                formStage.close();
                            } else {
                                authService.ajouterDemande(new DemandeAdmin(utilisateur, cible, lien, TypeDemande.AJOUT_PERSONNE));
                                authService.ajouterUtilisateur(cible);
                                String sujet = "Demande d'ajout d'une nouvelle personne";
                                String contenu = "Veuillez ajouter la personne suivante : " +
                                        prenomField.getText() + " " + nomField.getText() +
                                        ", née le " + datePicker.getValue() +
                                        ", " + natField.getText() + ", " + genreCombo.getValue() + ", " + lienCombo.getValue() + ".";
                                MailService.envoyerEmail("diffoglenn007@gmail.com", sujet, contenu);
                                Alert info = new Alert(Alert.AlertType.INFORMATION, "Demande envoyée à l’admin.");
                                info.show();
                                formStage.close();
                            }
                        }
                    });
                } else {
                    Personne cible = personCombo.getValue();
                    LienParente lien = lienCombo.getValue();
                    if (cible != null && lien != null) {
                        if (utilisateur.getCompte() instanceof Admin) {
                            AuthService.modifierPersonne(cible.getNom(), cible.getPrenom(), cible.getNationalite(), cible.getGenre());
                            Alert info = new Alert(Alert.AlertType.INFORMATION, "Informations modifiées!");
                            info.show();
                            formStage.close();
                        } else {
                            authService.ajouterDemande(new DemandeAdmin(utilisateur, cible, lien, TypeDemande.MODIFICATION_INFO));
                            String sujet = "Demande de modification de profil";
                            String contenu = "Veuillez modifier la personne " + cible.getPrenom() + " " +
                                    cible.getNom() + " (ID " + cible.getNss() + ") : " +
                                    "nouvelle nationalité = " + natField.getText() +
                                    ", nouveau genre = " + genreCombo.getValue() + ", nouveau lien = " + lienCombo.getValue() + ".";
                            MailService.envoyerEmail("diffoglenn007@gmail.com", sujet, contenu);
                            new Alert(Alert.AlertType.INFORMATION, "✅ Demande envoyée à l'admin.").show();
                            formStage.close();
                        }
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.").show();
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error processing form submission", ex);
                Alert error = new Alert(Alert.AlertType.ERROR,
                        "Une erreur est survenue lors de l'envoi. Veuillez réessayer.");
                error.setHeaderText("Erreur d'envoi");
                error.showAndWait();
            }
        });

        root.getChildren().addAll(actionRow, addFieldsBox, modifyBox, champsCommuns, envoyerBtn);
        formStage.setScene(new Scene(root, 1000, 1000));
        formStage.show();
    }
}
