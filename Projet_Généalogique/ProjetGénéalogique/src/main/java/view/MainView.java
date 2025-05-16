package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Personne;
import service.AuthService;


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

        if (utilisateur != null) {
            Button voirMonArbreBtn = new Button("Voir mon arbre familial");
            Button voirTousArbresBtn = new Button("Voir tous les arbres");

            // Lorsqu'on clique sur "Voir mon arbre familial"
            voirMonArbreBtn.setOnAction(e -> {
                AffichageArbre affichageArbre = new AffichageArbre(utilisateur, stage, layout);
                affichageArbre.afficher();
            });

            // "Voir tous les arbres" reste inchangé
            voirTousArbresBtn.setOnAction(e -> {
                AllTreesView allTreesView = new AllTreesView(authService, utilisateur);
                allTreesView.start(stage);
            });

            layout.getChildren().addAll(voirMonArbreBtn, voirTousArbresBtn);
        }

        if (utilisateur == null) {
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

        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Accueil");
        stage.show();
    }
}