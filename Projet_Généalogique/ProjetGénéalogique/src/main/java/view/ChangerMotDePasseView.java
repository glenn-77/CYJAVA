package view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entites.Personne;
import service.AuthService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * A JavaFX view that allows a user to change their password.
 * This view is especially triggered during the first login, where password change is mandatory.
 */
public class ChangerMotDePasseView {

    private final AuthService authService;
    private final Personne personne;

    /**
     * Constructs the view with the given authentication service and user.
     *
     * @param authService the service used to manage user data and authentication
     * @param personne    the user who needs to change their password
     */
    public ChangerMotDePasseView(AuthService authService, Personne personne) {
        this.authService = authService;
        this.personne = personne;
    }

    /**
     * Starts the password change interface.
     * Prompts the user to input and confirm a new password, ensuring it differs from the old one.
     * On success, the password is updated and the user is redirected to the login view.
     *
     * @param stage the JavaFX stage in which the view is displayed
     */
    public void start(Stage stage) {
        Label titre = new Label("🔐 Changement de mot de passe obligatoire");
        PasswordField nouveauMotDePasse = new PasswordField();
        nouveauMotDePasse.setPromptText("Nouveau mot de passe");
        PasswordField confirmation = new PasswordField();
        confirmation.setPromptText("Confirmer le mot de passe");

        Label message = new Label();

        Button valider = new Button("Valider");
        valider.setOnAction(e -> {
            String mdp = nouveauMotDePasse.getText();
            String confirm = confirmation.getText();

            if (mdp.isBlank() || !mdp.equals(confirm)) {
                message.setText("❌ Les mots de passe ne correspondent pas.");
                return;
            }

            if (mdp.equals(personne.getCompte().getMotDePasse())) {
                message.setText("Le mot de passe est identique au précédent. Veuillez en choisir un autre.");
                return;
            }

            this.personne.getCompte().setMotDePasse(mdp);
            this.personne.getCompte().setPremiereConnexion(false);
            authService.mettreAJourUtilisateur(personne);
            message.setText("✅ Mot de passe modifié avec succès. Redirection...");

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                LoginView login = new LoginView(authService);
                login.start(stage);
            });
            pause.play();
        });

        Button retourButton = new Button("Retour");
        retourButton.setOnAction(e -> {
            MainView accueil = new MainView(authService, personne);
            accueil.start(stage);
        });

        VBox layout = new VBox(15, titre, nouveauMotDePasse, confirmation, valider, message, retourButton);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 800, 850);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Changement de mot de passe");
        stage.show();
    }
}

