package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.AuthService;
import service.ConsultationStatsService;
import entites.Personne;

import java.util.Map;

public class AffichageConsultations {

    private final ConsultationStatsService statsService = new ConsultationStatsService();
    private final AuthService authService;

    public AffichageConsultations(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Affiche les statistiques d'utilisation pour un utilisateur donné, dans une fenêtre JavaFX.
     *
     * @param nssUtilisateur NSS de l'utilisateur connecté
     */
    public void afficherStatistiques(String nssUtilisateur) {
        Stage statistiqueStage = new Stage();
        statistiqueStage.setTitle("Statistiques de consultation - Mon arbre");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        // Titre principal
        Label titre = new Label("📊 Consultations de votre arbre généalogique");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        layout.getChildren().add(titre);

        // Ajouter les statistiques des fréquences mensuelles
        Map<String, Long> frequencesMensuelles = statsService.calculerFrequences(nssUtilisateur, true);
        VBox frequencesMensuellesBox = creerSectionStatistiques("🔹 Fréquences mensuelles :", frequencesMensuelles);
        layout.getChildren().add(frequencesMensuellesBox);

        // Ajouter les statistiques des fréquences annuelles
        Map<String, Long> frequencesAnnuelles = statsService.calculerFrequences(nssUtilisateur, false);
        VBox frequencesAnnuellesBox = creerSectionStatistiques("🔹 Fréquences annuelles :", frequencesAnnuelles);
        layout.getChildren().add(frequencesAnnuellesBox);

        // Ajouter les consultations par utilisateur
        Map<String, Long> consultationsParUtilisateur = statsService.recupererConsultationsParUtilisateur(nssUtilisateur);
        VBox consultationsParUtilisateurBox = creerSectionConsultations("🔹 Consultations par utilisateur :", consultationsParUtilisateur);
        layout.getChildren().add(consultationsParUtilisateurBox);

        // Encapsuler le contenu dans un ScrollPane pour gérer le débordement
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        // Ajouter un bouton de retour en bas
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.getChildren().add(scrollPane);

        Button retourButton = new Button("🔙 Retour");
        retourButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        retourButton.setOnAction(e -> statistiqueStage.close());
        root.getChildren().add(retourButton);

        // Charger la scène dans la fenêtre
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        statistiqueStage.setScene(scene);
        statistiqueStage.show();
    }

    /**
     * Crée une section de statistiques formatée (mensuelles ou annuelles).
     *
     * @param titre   Le titre de la section
     * @param donnees Les données de statistiques
     * @return Un container VBox contenant les données formatées
     */
    private VBox creerSectionStatistiques(String titre, Map<String, Long> donnees) {
        VBox sectionBox = new VBox(10);
        sectionBox.setAlignment(Pos.TOP_LEFT);

        Label sectionTitre = new Label(titre);
        sectionTitre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sectionBox.getChildren().add(sectionTitre);

        if (donnees.isEmpty()) {
            Label aucunResultat = new Label("  - Aucune donnée disponible.");
            sectionBox.getChildren().add(aucunResultat);
        } else {
            donnees.forEach((periode, count) -> {
                Label data = new Label("  - " + periode + " : " + count + " consultations");
                sectionBox.getChildren().add(data);
            });
        }
        return sectionBox;
    }

    /**
     * Crée une section pour afficher les consultations par utilisateur.
     *
     * @param titre   Le titre de la section
     * @param donnees Les données des consultations par utilisateur
     * @return Un container VBox contenant les données formatées
     */
    private VBox creerSectionConsultations(String titre, Map<String, Long> donnees) {
        VBox sectionBox = new VBox(10);
        sectionBox.setAlignment(Pos.TOP_LEFT);

        Label sectionTitre = new Label(titre);
        sectionTitre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sectionBox.getChildren().add(sectionTitre);

        if (donnees.isEmpty()) {
            Label aucunResultat = new Label("  - Aucune consultation enregistrée.");
            sectionBox.getChildren().add(aucunResultat);
        } else {
            donnees.forEach((nssConsultant, count) -> {
                String prenomNom = getPrenomNomFromNSS(nssConsultant);
                Label data = new Label("  - " + prenomNom + " : " + count + " consultations");
                sectionBox.getChildren().add(data);
            });
        }
        return sectionBox;
    }

    /**
     * Retourne le prénom et nom pour un NSS donné.
     *
     * @param nss NSS de l'utilisateur
     * @return Prénom et nom (ou "Inconnu" si l'utilisateur n'existe pas)
     */
    private String getPrenomNomFromNSS(String nss) {
        System.out.println("🔍 Recherche NSS : " + nss); // DEBUG
        if (authService != null) {
            Personne personne = authService.getPersonneParNSS(nss); // Utilise AuthService pour trouver la personne
            if (personne != null) {
                System.out.println("✅ Trouvé : " + personne.getPrenom() + " " + personne.getNom()); // DEBUG
                return personne.getPrenom() + " " + personne.getNom();
            }
        }
        System.out.println("❌ Introuvable pour NSS : " + nss); // DEBUG
        return "Inconnu";
    }
}