package view;

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
        statistiqueStage.setTitle("Consultation Statistics - My Family Tree");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Label titre = new Label("📊 Consultations of your family tree");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        layout.getChildren().add(titre);

        Map<String, Long> frequencesMensuelles = statsService.calculerFrequences(nssUtilisateur, true);
        VBox frequencesMensuellesBox = creerSectionStatistiques("🔹 Monthly Frequencies:", frequencesMensuelles);
        layout.getChildren().add(frequencesMensuellesBox);

        Map<String, Long> frequencesAnnuelles = statsService.calculerFrequences(nssUtilisateur, false);
        VBox frequencesAnnuellesBox = creerSectionStatistiques("🔹 Yearly Frequencies:", frequencesAnnuelles);
        layout.getChildren().add(frequencesAnnuellesBox);

        Map<String, Long> consultationsParUtilisateur = statsService.recupererConsultationsParUtilisateur(nssUtilisateur);
        VBox consultationsParUtilisateurBox = creerSectionConsultations("🔹 Consultations by user:", consultationsParUtilisateur);
        layout.getChildren().add(consultationsParUtilisateurBox);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.getChildren().add(scrollPane);

        Button retourButton = new Button("🔙 Back");
        retourButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        retourButton.setOnAction(e -> statistiqueStage.close());
        root.getChildren().add(retourButton);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
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

        Label sectionTitre = new Label(titre);
        sectionTitre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sectionBox.getChildren().add(sectionTitre);

        if (donnees.isEmpty()) {
            Label aucunResultat = new Label("  - No data available.");
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
     * Creates a section to display consultations by user.
     *
     * @param titre   the title of the section
     * @param donnees the data of consultations per user
     * @return a VBox container with the formatted data
     */
    private VBox creerSectionConsultations(String titre, Map<String, Long> donnees) {
        VBox sectionBox = new VBox(10);
        sectionBox.setAlignment(Pos.TOP_LEFT);

        Label sectionTitre = new Label(titre);
        sectionTitre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sectionBox.getChildren().add(sectionTitre);

        if (donnees.isEmpty()) {
            Label aucunResultat = new Label("  - No recorded consultations.");
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
     * Returns the full name of a user given their social security number.
     *
     * @param nss the social security number of the user
     * @return the user's full name, or "Unknown" if the user does not exist
     */
    private String getPrenomNomFromNSS(String nss) {
        System.out.println("🔍 Searching NSS: " + nss); // DEBUG
        if (authService != null) {
            Personne personne = authService.getPersonneParNSS(nss);
            if (personne != null) {
                System.out.println("✅ Found: " + personne.getPrenom() + " " + personne.getNom()); // DEBUG
                return personne.getPrenom() + " " + personne.getNom();
            }
        }
        System.out.println("❌ Not found for NSS: " + nss); // DEBUG
        return "Unknown";
    }
}
