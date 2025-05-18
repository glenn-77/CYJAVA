package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.AuthService;
import model.Personne;

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

            Button voirMonArbreBtn = new Button("🌳 Voir mon arbre familial");
            Button voirTousArbresBtn = new Button("👥 Voir tous les arbres");
            Button souvenirsBtn = new Button("📸 Souvenirs");
            Button logoutButton = new Button("🔴 Se déconnecter");

            voirMonArbreBtn.setOnAction(e -> {
                BorderPane arbreView = new BorderPane();
                Group arbreGroup = new Group();

                ScrollPane scrollPane = new ScrollPane(arbreGroup);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                arbreView.setCenter(scrollPane);

                AffichageArbre affichageArbre = new AffichageArbre(utilisateur, stage);
                affichageArbre.afficher(arbreGroup);

                Button retourBtn = new Button("🔙 Retour");
                retourBtn.setOnAction(event -> {
                    MainView retourAccueil = new MainView(authService, utilisateur);
                    retourAccueil.start(stage);
                });
                arbreView.setBottom(retourBtn);
                BorderPane.setMargin(retourBtn, new Insets(10));

                Scene arbreScene = new Scene(arbreView, 1400, 900);
                arbreScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(arbreScene);
            });

            voirTousArbresBtn.setOnAction(e -> {
                AllTreesView allTreesView = new AllTreesView();
                allTreesView.start(stage);
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

        } else {
            Label titleLabel = new Label("Bienvenue !");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            Button loginButton = new Button("Se connecter");
            Button registerButton = new Button("S'inscrire");

            loginButton.setOnAction(e -> {
                LoginView loginView = new LoginView(authService);
                loginView.start(stage);
            });

            registerButton.setOnAction(e -> {
                InscriptionView inscriptionView = new InscriptionView(authService);
                inscriptionView.start(stage);
            });

            layout.getChildren().addAll(titleLabel, loginButton, registerButton);
        }

        stage.setScene(scene);
        stage.setTitle("Accueil");
        stage.show();
    }
}
