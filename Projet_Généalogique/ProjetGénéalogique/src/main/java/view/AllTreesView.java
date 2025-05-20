package view;

import entites.ArbreGenealogique;
import entites.Personne;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.GlobalTreesManager;
import javafx.geometry.Pos; // Ajout de cet import spécifique pour gérer Pos
import java.util.List;

/**
 * Classe qui gère l'affichage de tous les arbres généalogiques.
 */
public class AllTreesView {

    private final Personne utilisateurConnecte;
    private final Stage stage;

    public AllTreesView(Personne utilisateurConnecte, Stage stage) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.stage = stage;
    }

    /**
     * Affiche la liste de tous les arbres généalogiques disponibles.
     */
    /**
     * Affiche la liste de tous les arbres généalogiques disponibles.
     */
    public void afficher() {
        // Charger les arbres depuis le fichier CSV (si nécessaire)
        if (GlobalTreesManager.getArbres().isEmpty()) {
            GlobalTreesManager.chargerArbresDepuisCSV();
        }

        // Vérification : l'utilisateur connecté est-il valide ?
        if (utilisateurConnecte == null) {
            System.out.println("Erreur : Aucun utilisateur connecté pour afficher la liste des arbres.");
            return;
        }

        List<ArbreGenealogique> arbres = GlobalTreesManager.getArbres();

        // Layout principal avec style similaire à MainView
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        // Ajouter un titre principal
        Label titre = new Label("👥 Liste des arbres généalogiques disponibles");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        layout.getChildren().add(titre);

        // Ajouter la liste des arbres ou un message si aucun n'est disponible
        if (arbres.isEmpty()) {
            Label aucunArbre = new Label("⛔ Aucun arbre généalogique disponible.");
            aucunArbre.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            layout.getChildren().add(aucunArbre);
        } else {
            // Ajouter les arbres dans un ScrollPane
            ScrollPane scrollPane = new ScrollPane();
            VBox arbresBox = new VBox(10);
            arbresBox.setAlignment(Pos.CENTER);

            for (ArbreGenealogique arbre : arbres) {
                String nomProprietaire = (arbre.getProprietaire() != null)
                        ? arbre.getProprietaire().getPrenom() + " " + arbre.getProprietaire().getNom()
                        : "Inconnu";

                Button arbreButton = new Button("Voir l'arbre de " + nomProprietaire);
                arbreButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                arbreButton.setOnAction(e -> afficherArbre(arbre));
                arbresBox.getChildren().add(arbreButton);
            }

            scrollPane.setContent(arbresBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(500); // Taille de la zone défilable
            layout.getChildren().add(scrollPane);
        }

        // Ajouter un bouton de retour
        Button retour = new Button("🔙 Retour au menu principal");
        retour.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        retour.setOnAction(e -> revenirAuMenuPrincipal());
        layout.getChildren().add(retour);

        // Créer et afficher la scène
        Scene scene = new Scene(layout, 800, 900);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Tous les arbres généalogiques");
        stage.show();
    }

    /**
     * Affiche un arbre spécifique.
     *
     * @param arbre L'arbre généalogique à afficher.
     */
    /**
     * Affiche un arbre spécifique.
     *
     * @param arbre L'arbre généalogique à afficher.
     */
    private void afficherArbre(ArbreGenealogique arbre) {
        // Enregistrer la consultation de l'arbre
        if (arbre != null) {
            arbre.consulterArbre(utilisateurConnecte.getNss());
        }

        BorderPane arbreView = new BorderPane();

        // Création de la vue de l'arbre
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
        retour.setOnAction(e -> afficher());
        arbreView.setBottom(retour);
        BorderPane.setMargin(retour, new Insets(10));

        // Créer une nouvelle scène
        Scene scene = new Scene(arbreView, 1400, 900);
        stage.setScene(scene);
        stage.setTitle("Arbre généalogique");
        stage.show();
    }

    /**
     * Revient au menu principal.
     */
    private void revenirAuMenuPrincipal() {
        new MainView(null, utilisateurConnecte).start(stage);
    }
}