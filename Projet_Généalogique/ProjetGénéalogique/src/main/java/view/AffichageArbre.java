package view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Personne;
import service.AuthService;
import java.util.HashSet;
import java.util.Set;

public class AffichageArbre {

    private final Personne utilisateurConnecte;
    private final Stage stage;
    private VBox mainLayout; // Layout principal facultatif
    private final Set<Personne> personnesAffichees = new HashSet<>();

    /**
     * Constructeur de base prenant uniquement l'utilisateur connecté et le Stage.
     */
    public AffichageArbre(Personne utilisateurConnecte, Stage stage) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.stage = stage;
    }

    /**
     * Constructeur surchargé pour inclure le layout principal.
     */
    public AffichageArbre(Personne utilisateurConnecte, Stage stage, VBox mainLayout) {
        this(utilisateurConnecte, stage); // Appelle le constructeur principal
        this.mainLayout = mainLayout;
    }

    public void afficher() {
        // Conteneur pour l'arbre généalogique
        Pane arbrePane = new Pane();
        arbrePane.setMinSize(1000, 800);
        arbrePane.setPadding(new Insets(20));

        // Conteneur centré
        StackPane arbreCentre = new StackPane(arbrePane);
        arbreCentre.setPadding(new Insets(10));

        // Dessiner l'arbre
        dessinerArbre(arbrePane, utilisateurConnecte, 500, 50, 300);

        // Bouton de retour
        Button retour = new Button("Retour");
        retour.setOnAction(e -> retournerAccueil());
        retour.setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");

        // Layout principal
        BorderPane root = new BorderPane();
        root.setCenter(arbreCentre);
        root.setBottom(retour);
        BorderPane.setMargin(retour, new Insets(10));

        if (mainLayout != null) {
            mainLayout.getChildren().clear();
            mainLayout.getChildren().add(root);
        } else {
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    private void retournerAccueil() {
        MainView mainView = new MainView(new AuthService(), utilisateurConnecte); // Passez les bons arguments ici
        mainView.start(stage); // Revenir à l'accueil sans fermer la fenêtre
    }

    /**
     * Dessine l'arbre généalogique d'une personne.
     *
     * @param pane     Conteneur de dessin
     * @param personne La personne connectée
     * @param x        Position X
     * @param y        Position Y
     * @param offset   Décalage horizontal
     */
    private void dessinerArbre(Pane pane, Personne personne, double x, double y, double offset) {
        if (personne == null || !personnesAffichees.add(personne)) {
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

            // Lien entre l'enfant et le père
            if (enfant.getPere() == personne) {
                Line lienPere = new Line(x + noeud.getBoundsInLocal().getWidth() / 2, y + 5, enfantX, enfantsY - 15);
                lienPere.setStroke(Color.BLUE);
                pane.getChildren().add(lienPere);
            }

            // Lien entre l'enfant et la mère
            if (enfant.getMere() == personne) {
                Line lienMere = new Line(x + noeud.getBoundsInLocal().getWidth() / 2, y + 5, enfantX, enfantsY - 15);
                lienMere.setStroke(Color.RED);
                pane.getChildren().add(lienMere);
            }

            // Dessiner l'enfant
            dessinerArbre(pane, enfant, enfantX, enfantsY, offset / 2);
            enfantIndex++;
        }
    }
}