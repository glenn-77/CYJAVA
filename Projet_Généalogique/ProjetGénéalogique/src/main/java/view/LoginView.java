package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Personne;
import javafx.geometry.Pos;
import service.AuthService;


public class LoginView {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion");

        VBox layout = new VBox(15); // Plus d'espace
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f4f4f4;"); // Fond gris clair

        Label titleLabel = new Label("Bienvenue !");
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
        loginButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-weight: bold;"
        );

        Button inscriptionButton = new Button("S'inscrire");
        inscriptionButton.setOnAction(e -> {
            InscriptionView inscriptionView = new InscriptionView(authService);
            inscriptionView.start(primaryStage);
        });


        layout.getChildren().add(inscriptionButton);


        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            Personne personne = authService.authentifier(email, password);
            if (personne != null) {
                // Rediriger vers la page d'accueil
                AccueilView accueilView = new AccueilView(personne);
                accueilView.start(primaryStage);
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Échec de la connexion.");
            }
        });

        layout.setAlignment(Pos.CENTER); // Centrage vertical/horizontal
        layout.getChildren().addAll(
                titleLabel,
                emailField,
                passwordField,
                loginButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 350, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
