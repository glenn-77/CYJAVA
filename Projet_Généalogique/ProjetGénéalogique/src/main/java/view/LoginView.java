package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Personne;
import service.AuthService;

public class LoginView {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Label titleLabel = new Label("Connectez-vous !");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Se connecter");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        Button inscriptionButton = new Button("S'inscrire");
        Button retourButton = new Button("Retour");

        inscriptionButton.setOnAction(e -> {
            InscriptionView inscriptionView = new InscriptionView(authService);
            inscriptionView.start(primaryStage);
        });

        retourButton.setOnAction(e -> {
            MainView mainView = new MainView(authService);
            mainView.start(primaryStage);
        });

        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            Personne personne = authService.authentifier(email, password);
            if (personne != null) {
                MainView accueil = new MainView(authService, personne);
                accueil.start(primaryStage);
            } else {
                messageLabel.setText("Échec de la connexion.");
            }
        });

        layout.getChildren().addAll(
                titleLabel, emailField, passwordField,
                loginButton, inscriptionButton, retourButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 350, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
