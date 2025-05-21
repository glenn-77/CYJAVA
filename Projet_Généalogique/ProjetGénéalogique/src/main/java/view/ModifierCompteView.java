package view;

import entites.enums.NiveauVisibilite;
import entites.Personne;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.AuthService;

/**
 * JavaFX view that allows a user to edit their account settings.
 * Users can update login credentials, contact info, visibility preferences, and password.
 */
public class ModifierCompteView {

    private final Personne utilisateur;
    private final AuthService authService;

    /**
     * Constructs the view with the authenticated user and authentication service.
     *
     * @param utilisateur the user whose account is being edited
     * @param authService the service to update account data
     */
    public ModifierCompteView(Personne utilisateur, AuthService authService) {
        this.utilisateur = utilisateur;
        this.authService = authService;
    }

    /**
     * Displays the modification form in a modal JavaFX window.
     * Fields include login, email, phone, visibility, and optional password change.
     *
     * @param parentStage the parent window to which this modal is attached
     */
    public void start(Stage parentStage) {
        Stage stage = new Stage();
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Modifier mon compte");
        stage.setMinWidth(500);
        stage.setMinHeight(650);
        stage.setResizable(true);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label nomLabel = new Label("Nom : " + utilisateur.getNom());
        Label prenomLabel = new Label("Prénom : " + utilisateur.getPrenom());

        TextField loginField = new TextField(utilisateur.getCompte().getLogin());
        TextField emailField = new TextField(utilisateur.getCompte().getEmail());
        TextField telField = new TextField(utilisateur.getCompte().getTelephone());

        ChoiceBox<NiveauVisibilite> visibiliteChoice = new ChoiceBox<>(FXCollections.observableArrayList(NiveauVisibilite.values()));
        visibiliteChoice.setValue(utilisateur.getNiveauVisibilite());

        PasswordField motDePasseField = new PasswordField();
        motDePasseField.setPromptText("Nouveau mot de passe (laisser vide si inchangé)");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirmer le mot de passe");

        Button enregistrerBtn = new Button("Enregistrer");

        enregistrerBtn.setOnAction(e -> {
            String login = loginField.getText().trim();
            String email = emailField.getText().trim();
            String tel = telField.getText().trim();
            String mdp = motDePasseField.getText().trim();
            String confirm = confirmField.getText().trim();
            NiveauVisibilite nv = visibiliteChoice.getValue();

            if (!mdp.isEmpty() && !mdp.equals(confirm)) {
                new Alert(Alert.AlertType.WARNING, "Les mots de passe ne correspondent pas.").show();
                return;
            }

            utilisateur.getCompte().setLogin(login);
            utilisateur.getCompte().setEmail(email);
            utilisateur.getCompte().setTelephone(tel);
            utilisateur.setNiveauVisibilite(nv);
            if (!mdp.isEmpty()) {
                utilisateur.getCompte().setMotDePasse(mdp);
            }

            try {
                    authService.mettreAJourUtilisateur(utilisateur);
                    new Alert(Alert.AlertType.INFORMATION, "Modifications enregistrées !").showAndWait();
                    stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Exception lors de l'enregistrement : " + ex.getMessage()).show();
            }
        });

        content.getChildren().addAll(
                nomLabel,
                prenomLabel,
                new Label("Login :"), loginField,
                new Label("Email :"), emailField,
                new Label("Téléphone :"), telField,
                new Label("Visibilité :"), visibiliteChoice,
                new Label("Mot de passe :"), motDePasseField,
                new Label("Confirmation :"), confirmField,
                enregistrerBtn
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(Region.USE_COMPUTED_SIZE);

        Scene scene = new Scene(scrollPane, 500, 650);
        stage.setScene(scene);
        stage.show();
    }
}
