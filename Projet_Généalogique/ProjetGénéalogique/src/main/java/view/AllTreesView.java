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

        // Si aucun arbre n'est disponible, afficher un message.
        if (arbres.isEmpty()) {
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            Label message = new Label("Aucun arbre généalogique disponible.");
            Button retour = new Button("Retour");
            retour.setOnAction(e -> revenirAuMenuPrincipal());
            layout.getChildren().addAll(message, retour);

            Scene scene = new Scene(layout, 400, 300);
            stage.setScene(scene);
            stage.setTitle("Tous les arbres généalogiques");
            stage.show();
            return;
        }

        // Layout principal pour afficher les arbres
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().add(new Label("Liste des arbres généalogiques disponibles :"));

        // Ajouter un bouton pour chaque arbre
        for (ArbreGenealogique arbre : arbres) {
            String nomProprietaire = (arbre.getProprietaire() != null)
                    ? arbre.getProprietaire().getPrenom() + " " + arbre.getProprietaire().getNom()
                    : "Inconnu";

            Button arbreButton = new Button("Voir l'arbre de " + nomProprietaire);
            arbreButton.setOnAction(e -> afficherArbre(arbre));
            layout.getChildren().add(arbreButton);
        }

        // Bouton pour revenir au menu principal
        Button retour = new Button("Retour");
        retour.setOnAction(e -> revenirAuMenuPrincipal());
        layout.getChildren().add(retour);

        // Créer la scène
        Scene scene = new Scene(layout, 800, 900);
        stage.setScene(scene);
        stage.setTitle("Tous les arbres généalogiques");
        stage.show();
    }

    /**
     * Affiche un arbre spécifique.
     *
     * @param arbre L'arbre généalogique à afficher.
     */
    private void afficherArbre(ArbreGenealogique arbre) {
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