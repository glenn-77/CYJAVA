package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Personne;


public class LoginView {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        Label messageLabel = new Label();

        Button loginButton = new Button("Se connecter");
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            Personne personne = authService.authentifier(email, password);
            if (personne != null) {
                messageLabel.setText("Connexion réussie. Bienvenue " + personne.getPrenom() + " !");
                // Tu peux enchaîner ici vers un écran d'accueil ou de navigation
            } else {
                messageLabel.setText("Échec de la connexion. Vérifie tes identifiants.");
            }
        });

        layout.getChildren().addAll(new Label("Email:"), emailField,
                                    new Label("Mot de passe:"), passwordField,
                                    loginButton, messageLabel);

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
