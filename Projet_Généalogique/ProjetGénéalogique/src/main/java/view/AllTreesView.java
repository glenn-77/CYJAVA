package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.ArbreGenealogique;
import model.Personne;
import service.AuthService;
import service.GlobalTreesManager;
import java.util.List;

public class AllTreesView {

    private final AuthService authService;
    private final Personne utilisateurConnecte; // Utilisateur actuel (au cas où nécessaire)

    public AllTreesView(AuthService authService, Personne utilisateurConnecte) {
        this.authService = authService;
        this.utilisateurConnecte = utilisateurConnecte;
    }

    /**
     * Lance l'affichage de tous les arbres généalogiques.
     *
     * @param stage La fenêtre principale à afficher.
     */
    public void start(Stage stage) {
        // Récupérer tous les arbres généalogiques
        List<ArbreGenealogique> arbres = authService.getArbres();

        Pane arbresPane = new Pane();
        arbresPane.setMinSize(1200, 800);

        // Décaler chaque arbre pour qu'il n'y ait pas de chevauchement
        double xStart = 50;
        double yStart = 50;
        double offsetX = 500;

        for (ArbreGenealogique arbre : arbres) {
            if (arbre.getProprietaire() != null) {
                dessinerArbre(arbresPane, arbre.getProprietaire(), xStart, yStart, 150);
                xStart += offsetX; // Décalage horizontal pour le prochain arbre
            }
        }

        // Conteneur centré
        StackPane arbreCentre = new StackPane(arbresPane);
        arbreCentre.setPadding(new Insets(20));

        // Bouton de retour
        Button retour = new Button("Retour");
        retour.setOnAction(e -> retournerAccueil(stage));

        // Layout principal
        BorderPane root = new BorderPane();
        root.setCenter(arbreCentre);
        root.setBottom(retour);
        BorderPane.setMargin(retour, new Insets(10));
        Scene scene = new Scene(root, 1400, 900);

        stage.setScene(scene);
        stage.setTitle("Tous les arbres généalogiques");
        stage.show();
    }

    /**
     * Retourne à l'accueil.
     */
    private void retournerAccueil(Stage stage) {
        MainView mainView = new MainView(authService, utilisateurConnecte);
        mainView.start(stage);
    }

    /**
     * Dessine un arbre généalogique d'une personne.
     *
     * @param pane     Conteneur de dessin
     * @param personne La personne connectée
     * @param x        Position X
     * @param y        Position Y
     * @param offset   Décalage horizontal
     */
    private void dessinerArbre(Pane pane, Personne personne, double x, double y, double offset) {
        if (personne == null) {
            return;
        }

        // Créer et positionner le nœud pour cette personne
        Text noeud = new Text(personne.getPrenom() + " " + personne.getNom());
        noeud.setX(x);
        noeud.setY(y);
        noeud.setFill(Color.BLACK);
        pane.getChildren().add(noeud);

        double enfantsY = y + 100; // Position verticale des enfants

        // Dessiner les liens avec les enfants
        int enfantIndex = 0;
        for (Personne enfant : personne.getEnfants()) {
            double enfantX = x - offset + (enfantIndex * (offset * 2.0 / (personne.getEnfants().size() + 1)));

            // Dessiner une ligne entre la personne et son enfant
            Line lien = new Line(x + noeud.getBoundsInLocal().getWidth() / 2, y + 5, enfantX, enfantsY - 15);
            lien.setStroke(Color.GRAY);
            pane.getChildren().add(lien);

            // Dessiner l'enfant
            dessinerArbre(pane, enfant, enfantX, enfantsY, offset / 2);
            enfantIndex++;
        }
    }

    public List<ArbreGenealogique> getArbres() {
        return GlobalTreesManager.getArbres(); // Retourne les arbres depuis GlobalTreesManager
    }
}