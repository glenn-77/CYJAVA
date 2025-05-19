package view;

import entites.LienParente;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entites.Genre;
import service.AuthService;
import entites.Personne;
import service.MailService;

import java.util.*;

public class MainView {

    private final AuthService authService;
    private final Personne utilisateur;

    public MainView(AuthService authService) {
        this(authService, null); // par défaut, pas encore connecté
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

        // Si l'utilisateur est connecté
        if (utilisateur != null) {
            Label bienvenue = new Label("Bienvenue " + utilisateur.getPrenom() + " !");
            bienvenue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Button voirMonArbreBtn = new Button("🌳 Voir mon arbre familial");
            Button voirTousArbresBtn = new Button("👥 Voir tous les arbres");
            Button souvenirsBtn = new Button("📸 Souvenirs");
            Button rechercheBtn = new Button("🔍 Rechercher une personne");
            Button logoutButton = new Button("🔴 Se déconnecter");

            // Lorsqu'on clique sur "Voir mon arbre familial"
            voirMonArbreBtn.setOnAction(e -> {
                BorderPane arbreView = new BorderPane();

                HBox topBar = new HBox();
                topBar.setAlignment(Pos.TOP_RIGHT);
                topBar.setPadding(new Insets(10));
                Button formButton = new Button("Ajouter/Modifier un nœud");
                topBar.getChildren().add(formButton);
                arbreView.setTop(topBar);  // Place le HBox en haut du BorderPane
                // Action du bouton pour ouvrir le formulaire
                formButton.setOnAction(event -> ouvrirFormulaire(stage));

                // Créer un conteneur graphique pour dessiner l'arbre de l'utilisateur
                Group arbreGroup = new Group();

                // Ajouter un ScrollPane comme parent (pour le défilement)
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setContent(arbreGroup);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

                arbreView.setCenter(scrollPane);

                // Dessiner l'arbre dans le Group
                AffichageArbre affichageArbre = new AffichageArbre(utilisateur, stage);
                affichageArbre.afficher(arbreGroup);

                // Bouton de retour à l'accueil
                Button retourBtn = new Button("🔙 Retour");
                retourBtn.setOnAction(event -> {
                    MainView retourAccueil = new MainView(authService, utilisateur);
                    retourAccueil.start(stage);
                });
                arbreView.setBottom(retourBtn);
                BorderPane.setMargin(retourBtn, new Insets(10));

                // Définir la nouvelle scène pour afficher l'arbre
                Scene arbreScene = new Scene(arbreView, 1400, 900);
                arbreScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(arbreScene);
            });

            // Lorsqu'on clique sur "Voir tous les arbres"
            voirTousArbresBtn.setOnAction(e -> {
                AllTreesView allTreesView = new AllTreesView(); // Utilisation du constructeur sans arguments
                allTreesView.start(stage); // Charge la vue pour tous les arbres
            });

            souvenirsBtn.setOnAction(e -> {
                SouvenirsView souvenirsView = new SouvenirsView(authService, utilisateur);
                souvenirsView.start(stage);
            });

            rechercheBtn.setOnAction(e -> {
                RechercheView rechercheView = new RechercheView(authService, utilisateur);
                rechercheView.start(stage);
            });

            logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
            logoutButton.setOnAction(e -> {
                MainView mainView = new MainView(authService);
                mainView.start(stage);
            });

            layout.getChildren().addAll(
                    bienvenue,
                    voirMonArbreBtn,
                    voirTousArbresBtn,
                    souvenirsBtn,
                    rechercheBtn,
                    logoutButton
            );
        }

        // Si aucun utilisateur n'est connecté
        if (utilisateur == null) {
            Label titleLabel = new Label("Bienvenue !");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            // Boutons pour se connecter ou s'inscrire
            Button loginButton = new Button("Se connecter");
            Button registerButton = new Button("S'inscrire");

            // Action sur le bouton "Se connecter"
            loginButton.setOnAction(e -> {
                LoginView loginView = new LoginView(authService);
                loginView.start(stage);
            });

            // Action sur le bouton "S'inscrire"
            registerButton.setOnAction(e -> {
                InscriptionView inscriptionView = new InscriptionView(authService);
                inscriptionView.start(stage);
            });

            layout.getChildren().addAll(titleLabel, loginButton, registerButton);
        }

        // Créer et afficher la scène principale
        stage.setScene(scene);
        stage.setTitle("Accueil");
        stage.show();
    }

    private void ouvrirFormulaire(Stage parentStage) {
        // Configuration de la nouvelle fenêtre modale
        Stage formStage = new Stage();
        formStage.initOwner(parentStage);
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.setTitle("Ajout / Modification de personne");

        // Conteneur principal du formulaire
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // 1. Choix de l'action (Ajout ou Modification)
        Label actionLabel = new Label("Action :");
        ChoiceBox<String> actionChoice = new ChoiceBox<>(
                FXCollections.observableArrayList("Ajout", "Modification"));
        actionChoice.setValue("Ajout");  // valeur par défaut
        HBox actionRow = new HBox(10, actionLabel, actionChoice);

        // 2. Champs pour l'ajout d'une personne
        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        DatePicker datePicker = new DatePicker();
        nomField.setPromptText("Nom");
        prenomField.setPromptText("Prénom");
        datePicker.setPromptText("Date de naissance");
        HBox nomRow = new HBox(5, new Label("Nom :"), nomField);
        HBox prenomRow = new HBox(5, new Label("Prénom :"), prenomField);
        HBox dateRow = new HBox(5, new Label("Date de naissance :"), datePicker);
        VBox addFieldsBox = new VBox(8, nomRow, prenomRow, dateRow);

        // 3. Champ de sélection pour la modification d'une personne existante
        ComboBox<Personne> personCombo = new ComboBox<>();
        personCombo.setPromptText("Sélectionnez la personne à modifier");
        // Charger la liste des personnes depuis le CSV
        Set<Personne> personnesDeMonArbre = utilisateur.getEnfants();
        personnesDeMonArbre.add(utilisateur.getMere());
        personnesDeMonArbre.add(utilisateur.getPere());
        List<Personne> proches = personnesDeMonArbre.stream()
                .filter(p -> {
                    LienParente lien = utilisateur.getLiens().get(p);
                    return lien == LienParente.FILS || lien == LienParente.FILLE
                            || lien == LienParente.PERE || lien == LienParente.MERE;
                })
                .toList();

        personCombo.setItems(FXCollections.observableArrayList(proches));
        // Afficher nom/prenom/date dans la liste (toString() de Personne le gère)
        HBox personRow = new HBox(5, new Label("Personne :"), personCombo);
        VBox modifyBox = new VBox(8, personRow);

        // 4. Champs communs (nationalité et genre) pour Ajout ou Modification
        TextField natField = new TextField();
        natField.setPromptText("Nationalité");
        ComboBox<Genre> genreCombo = new ComboBox<>(FXCollections.observableArrayList(Genre.values()));
        genreCombo.setPromptText("Genre");
        ComboBox<LienParente> lienCombo = new ComboBox<>(FXCollections.observableArrayList(LienParente.values()));
        HBox natRow = new HBox(5, new Label("Nationalité :"), natField);
        HBox genreRow = new HBox(5, new Label("Genre :"), genreCombo);
        HBox lienRow = new HBox(5, new Label("Lien :"), lienCombo);

        // Par défaut on affiche les champs d'ajout, pas ceux de modification
        modifyBox.setVisible(false);
        modifyBox.setManaged(false);
        // Réagir au choix Ajout/Modification pour afficher/masquer les champs appropriés
        actionChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean ajout = newVal.equals("Ajout");
            addFieldsBox.setVisible(ajout);
            addFieldsBox.setManaged(ajout);
            modifyBox.setVisible(!ajout);
            modifyBox.setManaged(!ajout);
            // Réinitialiser éventuellement les champs
            if (ajout) {
                nomField.setDisable(false);
                prenomField.setDisable(false);
                datePicker.setDisable(false);
                personCombo.getSelectionModel().clearSelection();
                nomField.clear();
                prenomField.clear();
                datePicker.setValue(null);
                natField.clear();
                genreCombo.getSelectionModel().clearSelection();
                lienCombo.getSelectionModel().clearSelection();
            } else {
                // En mode modification, désactiver nom/prenom/date (on ne les change pas)
                nomField.setDisable(true);
                prenomField.setDisable(true);
                datePicker.setDisable(true);
            }
        });

        // Remplir les champs nationalité/genre existants quand une personne est choisie pour modification
        personCombo.setOnAction(e -> {
            Personne selection = personCombo.getValue();
            if (selection != null) {
                // Pré-remplir nationalité et genre actuels de la personne
                natField.setText(selection.getNationalite());
                genreCombo.setValue(selection.getGenre());
                LienParente lien = utilisateur.getLiens().getOrDefault(selection, null);
                lienCombo.setValue(lien);
                // Afficher aussi son nom/prénom/date à titre d'information
                nomField.setText(selection.getNom());
                prenomField.setText(selection.getPrenom());
                datePicker.setValue(selection.getDateNaissance());
            }
        });

        // 5. Bouton Envoyer avec confirmation et traitement
        Button sendButton = new Button("Envoyer");
        sendButton.setOnAction(event -> {
            // Fenêtre de confirmation avant envoi
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Confirmer la soumission");
            confirm.setContentText("Voulez-vous envoyer cette demande à l'administrateur ?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (actionChoice.getValue().equals("Ajout")) {
                        // Construire le contenu du mail pour un ajout
                        String sujet = "Demande d'ajout d'une nouvelle personne";
                        String contenu = "Veuillez ajouter la personne suivante : " +
                                prenomField.getText() + " " + nomField.getText() +
                                ", née le " + datePicker.getValue() +
                                ", " + natField.getText() + ", " + genreCombo.getValue() + ", " + lienCombo.getValue() + ".";
                        MailService.envoyerEmail("diffoglenn007@gmail.com",sujet, contenu);
                    } else {
                        Personne cible = personCombo.getValue();
                        // Construire le contenu du mail pour une modification
                        String sujet = "Demande de modification de profil";
                        String contenu = "Veuillez modifier la personne " + cible.getPrenom() + " " +
                                cible.getNom() + " (ID " + cible.getNss() + ") : " +
                                "nouvelle nationalité = " + natField.getText() +
                                ", nouveau genre = " + genreCombo.getValue() + ", nouveau lien = " + lienCombo.getValue() + ".";
                        MailService.envoyerEmail("diffoglenn007@gmail.com",sujet, contenu);
                    }
                    // Confirmation visuelle à l'utilisateur
                    Alert info = new Alert(Alert.AlertType.INFORMATION,
                            "Votre demande a été envoyée à l'administrateur.");
                    info.setHeaderText(null);
                    info.showAndWait();
                    formStage.close();  // fermer le formulaire
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR,
                            "Une erreur est survenue lors de l'envoi. Veuillez réessayer.");
                    error.setHeaderText("Erreur d'envoi");
                    error.showAndWait();
                }
            }
        });

        // 6. Assembler tous les éléments du formulaire dans la fenêtre
        root.getChildren().addAll(actionRow, addFieldsBox, modifyBox, natRow, genreRow, lienRow, sendButton);
        formStage.setResizable(true);
        formStage.setScene(new Scene(root, 1000, 1200));
        formStage.show();
    }

}