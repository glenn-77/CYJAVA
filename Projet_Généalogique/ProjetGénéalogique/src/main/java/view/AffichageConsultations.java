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

/**
 * Displays consultation statistics for a user in a JavaFX interface.
 * The statistics include monthly and yearly view frequencies as well as user-specific access counts.
 */
public class AffichageConsultations {

    private final ConsultationStatsService statsService = new ConsultationStatsService();
    private final AuthService authService;
    private boolean isDarkMode = false;

    /**
     * Constructs a new AffichageConsultations with the given authentication service.
     *
     * @param authService the authentication service used to retrieve user data
     */
    public AffichageConsultations(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Displays consultation statistics for a given user in a JavaFX window.
     *
     * @param nssUtilisateur the social security number of the connected user
     */
    public void afficherStatistiques(String nssUtilisateur) {
        Stage statistiqueStage = new Stage();
        statistiqueStage.setTitle("Statistiques de consultation - Mon arbre");
        statistiqueStage.setMaximized(true);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("root");

        Label titre = new Label("\uD83D\uDCCA Consultations de votre arbre généalogique");
        titre.setId("titre-consultation");

        Label resume = new Label("\uD83D\uDD0D Résumé : 3 ce mois-ci, 3 cette année, 3 utilisateurs différents.");
        resume.setId("resume-label");

        layout.getChildren().addAll(resume, titre);

        Map<String, Long> frequencesMensuelles = statsService.calculerFrequences(nssUtilisateur, true);
        VBox frequencesMensuellesBox = creerSectionStatistiques("\uD83D\uDD39 Fréquences mensuelles :", frequencesMensuelles);
        layout.getChildren().add(frequencesMensuellesBox);

        Map<String, Long> frequencesAnnuelles = statsService.calculerFrequences(nssUtilisateur, false);
        VBox frequencesAnnuellesBox = creerSectionStatistiques("\uD83D\uDD39 Fréquences annuelles :", frequencesAnnuelles);
        layout.getChildren().add(frequencesAnnuellesBox);

        Map<String, Long> consultationsParUtilisateur = statsService.recupererConsultationsParUtilisateur(nssUtilisateur);
        VBox consultationsParUtilisateurBox = creerSectionConsultations("\uD83D\uDD39 Consultations par utilisateur :", consultationsParUtilisateur);
        layout.getChildren().add(consultationsParUtilisateurBox);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        // Ajouter un bouton de retour en bas
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.getChildren().add(scrollPane);
        root.getStyleClass().add("root");

        Button retourButton = new Button("Retour");
        retourButton.setId("retour-button");
        retourButton.setOnAction(e -> statistiqueStage.close());

        Button themeButton = new Button("Mode sombre");
        themeButton.setId("theme-button");
        themeButton.setOnAction(e -> {
            if (isDarkMode) {
                root.getStyleClass().remove("dark-mode");
                layout.getStyleClass().remove("dark-mode");
                themeButton.setText("Mode sombre");
                isDarkMode = false;
            } else {
                root.getStyleClass().add("dark-mode");
                layout.getStyleClass().add("dark-mode");
                themeButton.setText("Mode clair");
                isDarkMode = true;
            }
        });

        root.getChildren().addAll(retourButton, themeButton);

        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/consultation.css").toExternalForm());
        statistiqueStage.setScene(scene);
        statistiqueStage.show();
    }

    /**
     * Creates a formatted statistics section (monthly or yearly).
     *
     * @param titre   the title of the section
     * @param donnees the statistics data to display
     * @return a VBox container with the formatted data
     */
    private VBox creerSectionStatistiques(String titre, Map<String, Long> donnees) {
        VBox sectionBox = new VBox(10);
        sectionBox.setAlignment(Pos.TOP_LEFT);
        sectionBox.setId("section-box");

        Label sectionTitre = new Label(titre);
        sectionTitre.getStyleClass().add("section-title");
        sectionBox.getChildren().add(sectionTitre);

        if (donnees.isEmpty()) {
            sectionBox.getChildren().add(new Label("  - Aucune donnée disponible."));
        } else {
            donnees.forEach((periode, count) -> {
                Label data = new Label("  - " + periode + " : " + count + " consultations");
                sectionBox.getChildren().add(data);
            });
        }
        return sectionBox;
    }

    /**
     * Creates a section to display consultations by user.
     *
     * @param titre   the title of the section
     * @param donnees the data of consultations per user
     * @return a VBox container with the formatted data
     */
    private VBox creerSectionConsultations(String titre, Map<String, Long> donnees) {
        VBox sectionBox = new VBox(10);
        sectionBox.setAlignment(Pos.TOP_LEFT);
        sectionBox.setId("section-box");

        Label sectionTitre = new Label(titre);
        sectionTitre.getStyleClass().add("section-title");
        sectionBox.getChildren().add(sectionTitre);

        if (donnees.isEmpty()) {
            sectionBox.getChildren().add(new Label("  - Aucune consultation enregistrée."));
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
     * Returns the full name of a user given their social security number.
     *
     * @param nss the social security number of the user
     * @return the user's full name, or "Unknown" if the user does not exist
     */
    private String getPrenomNomFromNSS(String nss) {
        System.out.println("🔍 Recherche NSS : " + nss); // DEBUG
        if (authService != null) {
            Personne personne = authService.getPersonneParNSS(nss);
            if (personne != null) {
                System.out.println("✅ Trouvé : " + personne.getPrenom() + " " + personne.getNom()); // DEBUG
                return personne.getPrenom() + " " + personne.getNom();
            }
        }
        return "Inconnu";
    }
}