package view;

import entites.Admin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entites.Personne;
import service.AuthService;

/**
 * A JavaFX view that handles user authentication (login).
 * Supports login via email, username, or private code.
 * Redirects users to the appropriate screen based on their validation and login state.
 */
public class LoginView {

    private final AuthService authService;

    /**
     * Constructs the LoginView with the provided authentication service.
     *
     * @param authService the authentication service used to validate users
     */
    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Starts the login scene in the provided JavaFX stage.
     * Allows users to log in, register, or return to the home screen.
     * Handles login failures, unvalidated users, and first-time logins with password change redirection.
     *
     * @param primaryStage the JavaFX stage on which the login UI will be displayed
     */
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connexion");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Label titleLabel = new Label("Connectez-vous !");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField identifiantField = new TextField();
        identifiantField.setPromptText("Email, login ou code privée");
        identifiantField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Se connecter");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        Button inscriptionButton = new Button("S'inscrire");
        Button retourButton = new Button("Retour");

        // Redirect to registration screen
        inscriptionButton.setOnAction(e -> {
            InscriptionView inscriptionView = new InscriptionView(authService);
            inscriptionView.start(primaryStage);
        });

        // Redirect to home screen
        retourButton.setOnAction(e -> {
            MainView mainView = new MainView(authService);
            mainView.start(primaryStage);
        });

        // Attempt authentication
        loginButton.setOnAction(e -> {
            String identifiant = identifiantField.getText();
            String password = passwordField.getText();
            Personne personne = authService.authentifier(identifiant, password);
            if (personne != null) {
                if (personne.getCompte() instanceof Admin) {
                    MainView accueil = new MainView(authService, personne);
                    accueil.start(primaryStage);
                }
                if (!personne.isValideParAdmin()) {
                    messageLabel.setText("Votre inscription est en attente de validation par l’administrateur.");
                    return;
                }

                if (personne.getCompte().isPremiereConnexion()) {
                    // Redirect to password change screen
                    ChangerMotDePasseView changerView = new ChangerMotDePasseView(authService, personne);
                    changerView.start(primaryStage);
                    return;
                }
                // Redirect to main application
                MainView accueil = new MainView(authService, personne);
                accueil.start(primaryStage);
            } else {
                messageLabel.setText("Échec de la connexion.");
            }
        });

        layout.getChildren().addAll(
                titleLabel, identifiantField, passwordField,
                loginButton, inscriptionButton, retourButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 850, 850);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
