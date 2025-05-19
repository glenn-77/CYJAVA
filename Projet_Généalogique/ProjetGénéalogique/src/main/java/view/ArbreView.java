package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entites.Personne;
import service.AuthService;
import entites.ArbreGenealogique;
import java.util.Set;


public class ArbreView {
    private final ArbreGenealogique arbre;
    private final Personne utilisateur;

    public ArbreView(ArbreGenealogique arbre, Personne utilisateur) {
        this.arbre = arbre;
        this.utilisateur = utilisateur;
    }

    public void start(Stage stage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);

        Label titre = new Label("Arbre généalogique");
        layout.getChildren().add(titre);

        Set<Personne> membresVisibles = arbre.afficherArbrePour(utilisateur);
        for (Personne p : membresVisibles) {
            Label personneLabel = new Label(p.getPrenom() + " " + p.getNom() + " (" + p.getLien() + ")");
            layout.getChildren().add(personneLabel);
        }

        Button retour = new Button("Retour");
        retour.setOnAction(e -> {
            new MainView(new AuthService(), utilisateur).start(stage);  // À adapter
        });

        layout.getChildren().add(retour);

        Scene scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Mon Arbre");
        stage.show();
    }
}
