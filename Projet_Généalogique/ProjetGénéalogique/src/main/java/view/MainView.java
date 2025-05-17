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
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.Group;

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
                BorderPane arbreView = new BorderPane();

                // Utiliser un Group comme conteneur pour le dessin
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

                // Bouton de retour
                Button retourBtn = new Button("Retour");
                retourBtn.setOnAction(event -> start(stage));
                arbreView.setBottom(retourBtn);
                BorderPane.setMargin(retourBtn, new Insets(10));

                Scene arbreScene = new Scene(arbreView, 1400, 900);
                stage.setScene(arbreScene);
            });

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