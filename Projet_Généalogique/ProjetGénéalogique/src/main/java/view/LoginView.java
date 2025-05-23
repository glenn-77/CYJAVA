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
    private boolean isDarkMode = false;

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
        layout.getStyleClass().add("root");

        Label titleLabel = new Label("Connectez-vous !");
        titleLabel.setId("title");

        TextField identifiantField = new TextField();
        identifiantField.setPromptText("Email, login ou code privée");
        identifiantField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.getStyleClass().add("password-field");

        Label messageLabel = new Label();
        messageLabel.setId("error-label");

        Button loginButton = new Button("Se connecter");
        loginButton.setId("login-button");

        Button inscriptionButton = new Button("S'inscrire");
        Button retourButton = new Button("Retour");

        Button themeButton = new Button("Mode sombre");
        themeButton.setOnAction(e -> {
            if (isDarkMode) {
                layout.getStyleClass().remove("dark-mode");
                themeButton.setText("Mode sombre");
                isDarkMode = false;
            } else {
                layout.getStyleClass().add("dark-mode");
                themeButton.setText("Mode clair");
                isDarkMode = true;
            }
        });

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
                    // Redirige vers une vue de changement de mot de passe
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
                titleLabel,
                identifiantField,
                passwordField,
                loginButton,
                inscriptionButton,
                retourButton,
                themeButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 850, 850);
        scene.getStylesheets().add(getClass().getResource("/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
