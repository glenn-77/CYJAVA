package view;

import entites.LienParente;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entites.Genre;
import service.AuthService;
import entites.Personne;
import service.MailService;
import service.DemandeAdminService;
import service.DemandeAdminService.DemandeAdmin;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MainView {

    private final AuthService authService;
    private final Personne utilisateur;

    public MainView(AuthService authService) {
        this(authService, null);
    }

    public MainView(AuthService authService, Personne utilisateur) {
        this.authService = authService;
        this.utilisateur = utilisateur;
    }

    public void start(Stage stage) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 800, 900);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        if (utilisateur != null) {
            Label bienvenue = new Label("Bienvenue " + utilisateur.getPrenom() + " !");
            bienvenue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            // 📸 Photo de profil
            ImageView imageView = new ImageView();
            imageView.setFitHeight(150);
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);

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
                e.printStackTrace();
            }

            // 🖼 Modifier la photo
            Button modifierPhotoBtn = new Button("🖼 Modifier ma photo");
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

            Button voirMonArbreBtn = new Button("🌳 Voir mon arbre familial");
            Button voirTousArbresBtn = new Button("👥 Voir tous les arbres");
            Button souvenirsBtn = new Button("📸 Souvenirs");
            Button rechercheBtn = new Button("🔍 Rechercher une personne");
            Button logoutButton = new Button("🔴 Se déconnecter");

            layout.getChildren().addAll(
                    bienvenue,
                    imageView,
                    modifierPhotoBtn,
                    voirMonArbreBtn,
                    voirTousArbresBtn,
                    souvenirsBtn,
                    rechercheBtn
            );

            if (utilisateur.getCompte().getEmail().equalsIgnoreCase("diffoglenn007@gmail.com")) {
                Button voirDemandesBtn = new Button("📬 Voir les demandes");
                voirDemandesBtn.setOnAction(e -> {
                    new DemandesAdminView(authService, utilisateur).start(stage);
                });
                layout.getChildren().add(voirDemandesBtn);
            }

            logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
            logoutButton.setOnAction(e -> new MainView(authService).start(stage));

            layout.getChildren().add(logoutButton);

            voirMonArbreBtn.setOnAction(e -> {
                BorderPane arbreView = new BorderPane();

                HBox topBar = new HBox();
                topBar.setAlignment(Pos.TOP_RIGHT);
                topBar.setPadding(new Insets(10));
                Button formButton = new Button("Ajouter/Modifier un nœud");
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

                Scene arbreScene = new Scene(arbreView, 1400, 900);
                arbreScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(arbreScene);
            });

            voirTousArbresBtn.setOnAction(e -> new AllTreesView().start(stage));
            souvenirsBtn.setOnAction(e -> new SouvenirsView(authService, utilisateur).start(stage));
            rechercheBtn.setOnAction(e -> new RechercheView(authService, utilisateur).start(stage));

        } else {
            Label titleLabel = new Label("Bienvenue !");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            Button loginButton = new Button("Se connecter");
            Button registerButton = new Button("S'inscrire");

            loginButton.setOnAction(e -> new LoginView(authService).start(stage));
            registerButton.setOnAction(e -> new InscriptionView(authService).start(stage));

            layout.getChildren().addAll(titleLabel, loginButton, registerButton);
        }

        stage.setScene(scene);
        stage.setTitle("Accueil");
        stage.show();
    }

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
                    });
                } else {
                    Personne cible = personCombo.getValue();
                    LienParente lien = lienCombo.getValue();
                    if (cible != null && lien != null) {
                        DemandeAdminService.ajouterDemande(new DemandeAdmin(utilisateur, cible, lien));
                        String sujet = "Demande de modification de profil";
                        String contenu = "Veuillez modifier la personne " + cible.getPrenom() + " " +
                                cible.getNom() + " (ID " + cible.getNss() + ") : " +
                                "nouvelle nationalité = " + natField.getText() +
                                ", nouveau genre = " + genreCombo.getValue() + ", nouveau lien = " + lienCombo.getValue() + ".";
                        MailService.envoyerEmail("diffoglenn007@gmail.com", sujet, contenu);
                        new Alert(Alert.AlertType.INFORMATION, "✅ Demande envoyée à l'admin.").show();
                        formStage.close();
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.").show();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR,
                        "Une erreur est survenue lors de l'envoi. Veuillez réessayer.");
                error.setHeaderText("Erreur d'envoi");
                error.showAndWait();
            }
        });

        root.getChildren().addAll(actionRow, addFieldsBox, modifyBox, champsCommuns, envoyerBtn);
        formStage.setScene(new Scene(root, 500, 600));
        formStage.show();
    }
}
