package view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Personne;

public class AccueilView {

    private final Personne utilisateur;

    public AccueilView(Personne utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void start(Stage stage) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 30; -fx-alignment: center;");

        Label welcome = new Label("Bienvenue " + utilisateur.getPrenom() + " !");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        layout.getChildren().add(welcome);

        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Accueil");
        stage.show();
    }
}
