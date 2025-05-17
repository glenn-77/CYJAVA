package view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Personne;
import service.AuthService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


public class ChangerMotDePasseView {

    private final AuthService authService;
    private final Personne personne;

    public ChangerMotDePasseView(AuthService authService, Personne personne) {
        this.authService = authService;
        this.personne = personne;
    }

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

            personne.getCompte().setMotDePasse(mdp);
            personne.getCompte().setPremiereConnexion(false);
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
        stage.setScene(scene);
        stage.setTitle("Changement de mot de passe");
        stage.show();
    }
}

