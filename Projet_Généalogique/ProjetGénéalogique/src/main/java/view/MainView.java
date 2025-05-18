package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import service.AuthService;
import model.Personne;

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
            Button logoutButton = new Button("🔴 Se déconnecter");

            // Lorsqu'on clique sur "Voir mon arbre familial"
            voirMonArbreBtn.setOnAction(e -> {
                BorderPane arbreView = new BorderPane();

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
}