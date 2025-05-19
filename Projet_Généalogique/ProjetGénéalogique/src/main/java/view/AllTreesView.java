package view;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entites.ArbreGenealogique;
import service.GlobalTreesManager;

import java.util.List;

/**
 * Vue principale pour afficher la liste des arbres généalogiques.
 */
public class AllTreesView {

    public void start(Stage stage) {
        List<ArbreGenealogique> arbres = GlobalTreesManager.getArbres();
        if (arbres.isEmpty()) {
            System.out.println("❌ Aucun arbre à afficher.");

            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            Label label = new Label("Aucun arbre généalogique disponible.");
            Button retour = new Button("Retour");
            retour.setOnAction(e -> stage.close());
            layout.getChildren().addAll(label, retour);

            Scene scene = new Scene(layout, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Tous les arbres");
            stage.show();
            return;
        }

        // Layout principal contenant la liste des arbres
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().add(new Label("Liste des arbres généalogiques :"));

        for (ArbreGenealogique arbre : arbres) {
            // Afficher un bouton pour chaque arbre
            String nomProprietaire = arbre.getProprietaire() != null
                    ? arbre.getProprietaire().getPrenom() + " " + arbre.getProprietaire().getNom()
                    : "Inconnu";
            Button arbreButton = new Button("Voir l'arbre de " + nomProprietaire);
            arbreButton.setOnAction(e -> afficherArbre(stage, arbre));
            layout.getChildren().add(arbreButton);
        }

        // Bouton de retour
        Button retour = new Button("Retour");
        retour.setOnAction(e -> start(stage));
        layout.getChildren().add(retour);

        Scene scene = new Scene(layout, 800, 900);
        stage.setScene(scene);
        stage.setTitle("Liste des arbres");
        stage.show();
    }

    /**
     * Affiche un arbre spécifique dans une nouvelle vue.
     *
     * @param stage Stage principal.
     * @param arbre L'arbre généalogique à afficher.
     */
    private void afficherArbre(Stage stage, ArbreGenealogique arbre) {
        BorderPane arbreView = new BorderPane();

        // Contenu de l'arbre
        if (arbre.getProprietaire() != null) {
            Group arbreGroup = new Group();
            AffichageArbre affichageArbre = new AffichageArbre(arbre.getProprietaire(), stage);
            affichageArbre.afficher(arbreGroup);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(arbreGroup);
            arbreView.setCenter(scrollPane);
        } else {
            arbreView.setCenter(new Label("Arbre sans propriétaire."));
        }

        // Bouton de retour
        Button retour = new Button("Retour");
        retour.setOnAction(e -> start(stage));
        arbreView.setBottom(retour);
        BorderPane.setMargin(retour, new Insets(10));

        Scene scene = new Scene(arbreView, 1400, 900);
        stage.setScene(scene);
        stage.setTitle("Arbre généalogique");
        stage.show();
    }
}