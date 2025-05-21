package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entites.Personne;
import service.AuthService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A JavaFX view for displaying and uploading family memories such as photos and videos.
 * Each family has a shared folder where all users with the same family ID can view and contribute media.
 */
public class SouvenirsView {

    private final Personne utilisateur;
    private final AuthService authService;
    private final File dossierCommun;

    /**
     * Constructs the SouvenirsView using the given authentication service and logged-in user.
     * Creates a shared directory for the user's family if it does not already exist.
     *
     * @param authService the authentication service
     * @param utilisateur the currently logged-in user
     */
    public SouvenirsView(AuthService authService, Personne utilisateur) {
        this.utilisateur = utilisateur;
        this.authService = authService;

        // Create the shared directory based on the user's family ID
        String familleId = utilisateur.getFamilleId();
        this.dossierCommun = new File("souvenirs/famille_" + familleId);

        if (!dossierCommun.exists()) {
            dossierCommun.mkdirs();
        }
    }

    /**
     * Starts the JavaFX scene to view and upload family memories.
     * Users can see images and videos stored in the shared folder and upload new ones.
     *
     * @param stage the main JavaFX stage
     */
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        Label title = new Label("📸 Galerie de souvenirs de la famille");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox topBox = new VBox(title);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        root.setTop(topBox);

        FlowPane galerie = new FlowPane();
        galerie.setPadding(new Insets(10));
        galerie.setHgap(15);
        galerie.setVgap(15);
        ScrollPane scrollPane = new ScrollPane(galerie);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        afficherSouvenirs(galerie);

        // Upload button
        Button uploadButton = new Button("Ajouter un souvenir");
        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir un souvenir");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images et vidéos", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.mp4")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    File destination = new File(dossierCommun, file.getName());
                    Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    afficherSouvenirs(galerie);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout du fichier.").showAndWait();
                }
            }
        });

        // Return button
        Button retourButton = new Button("Retour");
        retourButton.setOnAction(e -> {
            MainView mainView = new MainView(authService, utilisateur);
            mainView.start(stage);
        });

        HBox bottomBox = new HBox(20, uploadButton, retourButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Souvenirs partagés");
        stage.show();
    }

    /**
     * Loads and displays all the images and videos from the shared folder.
     *
     * @param galerie the FlowPane container where media elements will be added
     */
    private void afficherSouvenirs(FlowPane galerie) {
        galerie.getChildren().clear();
        File[] fichiers = dossierCommun.listFiles();
        if (fichiers == null) return;

        for (File file : fichiers) {
            if (file.getName().toLowerCase().endsWith(".mp4")) {
                Label label = new Label("🎥 Vidéo : " + file.getName());
                label.setStyle("-fx-font-size: 14px;");
                galerie.getChildren().add(label);
            } else {
                try {
                    ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                    imageView.setFitWidth(160);
                    imageView.setFitHeight(120);
                    imageView.setPreserveRatio(true);
                    galerie.getChildren().add(imageView);
                } catch (Exception e) {
                    galerie.getChildren().add(new Label("❌ Erreur d'affichage : " + file.getName()));
                }
            }
        }
    }
}
