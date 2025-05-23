package view;

import entites.Admin;
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
import service.AuthService;
import service.GlobalTreesManager;
import javafx.geometry.Pos;
import java.util.List;
import entites.enums.NiveauVisibilite;

/**
 * A JavaFX view that displays all genealogical trees visible to the connected user.
 * It supports filtering based on visibility rules and rendering the selected tree.
 */
public class AllTreesView {

    private final Personne utilisateurConnecte;
    private final Stage stage;

    /**
     * Constructs the view with the connected user and the primary JavaFX stage.
     *
     * @param utilisateurConnecte the currently logged-in user
     * @param stage               the JavaFX stage for rendering
     */
    public AllTreesView(Personne utilisateurConnecte, Stage stage) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.stage = stage;
    }

    /**
     * Displays the list of all genealogical trees that are visible to the connected user.
     * Applies visibility filters based on each tree's access level (public, protected, or private).
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

        // Liste des arbres disponibles
        List<ArbreGenealogique> arbres = GlobalTreesManager.getArbres();

        // Filtrage des arbres visibles
        List<ArbreGenealogique> arbresVisibles = arbres.stream()
                .filter(arbre -> {
                    Personne proprietaire = arbre.getProprietaire();
                    if (utilisateurConnecte.getCompte() instanceof Admin) return true;
                    return switch (proprietaire.getNiveauVisibilite()) {
                        case PUBLIQUE -> true; // Toujours visible
                        case PROTEGEE ->
                                arbre.contient(utilisateurConnecte); // Visible uniquement si l'utilisateur est dans l'arbre
                        case PRIVEE ->
                                proprietaire.equals(utilisateurConnecte); // Visible uniquement pour le propriétaire lui-même
                    };
                })
                .toList();

        // Layout principal avec style similaire à MainView
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        // Ajouter un titre principal
        Label titre = new Label("👥 Liste des arbres généalogiques disponibles");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        layout.getChildren().add(titre);

        // Ajouter la liste des arbres ou un message si aucun n'est disponible
        if (arbresVisibles.isEmpty()) {
            Label aucunArbre = new Label("⛔ Aucun arbre généalogique disponible.");
            aucunArbre.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            layout.getChildren().add(aucunArbre);
        } else {
            // Ajouter les arbres dans un ScrollPane
            ScrollPane scrollPane = new ScrollPane();
            VBox arbresBox = new VBox(10);
            arbresBox.setAlignment(Pos.CENTER);

            for (ArbreGenealogique arbre : arbresVisibles) {

                String nomProprietaire;

                // Vérifier le niveau de visibilité du propriétaire et ajuster l'affichage
                Personne proprietaire = arbre.getProprietaire();
                if (!proprietaire.isEstInscrit()) continue;
                if (proprietaire.getNiveauVisibilite() == NiveauVisibilite.PRIVEE && !proprietaire.equals(utilisateurConnecte) && !(utilisateurConnecte.getCompte() instanceof Admin)) {
                    nomProprietaire = "Inconnu"; // Propriétaire privé et différent de l'utilisateur connecté
                } else {
                    nomProprietaire = proprietaire.getPrenom() + " " + proprietaire.getNom(); // Nom complet si visible
                }

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
     * Displays a specific genealogical tree in a scrollable pane.
     *
     * @param arbre the tree to display
     */
    private void afficherArbre(ArbreGenealogique arbre) {

        if (arbre != null) {
            arbre.consulterArbre(utilisateurConnecte.getNss(), utilisateurConnecte);
        }

        BorderPane arbreView = new BorderPane();

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

        // Return button
        Button retour = new Button("Retour");
        retour.setOnAction(e -> afficher());
        arbreView.setBottom(retour);
        BorderPane.setMargin(retour, new Insets(10));

        Scene scene = new Scene(arbreView, 1400, 900);
        stage.setScene(scene);
        stage.setTitle("Arbre généalogique");
        stage.show();
    }

    /**
     * Navigates back to the main menu.
     */
    private void revenirAuMenuPrincipal() {
        new MainView(new AuthService(), utilisateurConnecte).start(stage);
    }
}