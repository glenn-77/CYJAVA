package view;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import service.AuthService;
import dao.UserDAO;
import entites.Personne;

import java.io.*;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A JavaFX view that provides a unified search interface for finding people by name, surname, or social security number (NSS).
 * Allows the user to view matching results and display associated photos.
 */
public class RechercheView {

    private final AuthService authService;
    private final Personne utilisateur;
    final Personne[] personneCourante = {null};

    /**
     * Constructs a RechercheView instance.
     *
     * @param authService the authentication service
     * @param utilisateur the currently logged-in user
     */
    public RechercheView(AuthService authService, Personne utilisateur) {
        this.authService = authService;
        this.utilisateur = utilisateur;
    }

    /**
     * Launches the unified search interface.
     * Enables the user to search by name, first name, or NSS and display a photo for the selected person.
     *
     * @param stage the JavaFX stage in which the scene is set
     */
    public void start(Stage stage) {
        TextField champRecherche = new TextField();
        champRecherche.setPromptText("Entrez un nom, prénom ou NSS");

        ComboBox<String> filtre = new ComboBox<>();
        filtre.getItems().addAll("Nom", "Prénom", "NSS");
        filtre.setValue("Nom");

        Button boutonPhoto = new Button("📷 Afficher la photo");
        ListView<Label> listeResultats = new ListView<>();

        // Zone d'affichage de la photo
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        // Dynamic search with filters
        champRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            String critere = filtre.getValue();
            listeResultats.getItems().clear();
            imageView.setImage(null);

            if (newValue.isEmpty()) return;

            Set<Personne> resultats = switch (critere) {
                case "Nom" -> UserDAO.chercherParNomContient(newValue);
                case "Prénom" -> UserDAO.chercherParPrenomContient(newValue);
                case "NSS" -> UserDAO.chercherParNSSContient(newValue);
                default -> Set.of();
            };

            if (resultats.isEmpty()) {
                listeResultats.getItems().add(new Label("Aucun résultat pour \"" + newValue + "\""));
            } else {
                for (Personne p : resultats) {
                    String ligne = switch (critere) {
                        case "Nom" -> surligner(p.getNom(), newValue) + " | Prénom : " + p.getPrenom();
                        case "Prénom" -> "Nom : " + p.getNom() + " | " + surligner(p.getPrenom(), newValue);
                        case "NSS" -> "Nom : " + p.getNom() + " | " + "Prénom : " + p.getPrenom() + " | " + "NSS : " + surligner(p.getNss(), newValue);
                        default -> "";
                    };
                    Label label = formatterLigneAvecSurlignage(ligne);
                    label.setOnMouseClicked(event -> personneCourante[0] = p);
                    listeResultats.getItems().add(label);
                }
            }
        });

        // Show photo button
        boutonPhoto.setOnAction(e -> {
            Personne p = personneCourante[0];
            if (p == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune personne sélectionnée.");
                alert.show();
                return;
            }

            try {
                String chemin = "Projet_Généalogique/ProjetGénéalogique/ressources/" + p.getUrlPhoto();
                System.out.println("📸 Chargement image depuis : " + chemin);
                Image image = new Image(new FileInputStream(chemin));
                imageView.setImage(image);

                if (image.isError()) {
                    System.err.println("❌ Erreur de chargement de l'image !");
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Erreur lors du chargement de l'image.");
                    alert.show();
                }

                if (imageView.getImage() == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Image non chargée.");
                    alert.show();
                }

            } catch (FileNotFoundException ex) {
                try {
                    System.out.println("Fichier par défaut utilisé");
                    Image defaultImg = new Image(new FileInputStream("Projet_Généalogique/ProjetGénéalogique/ressources/images/default.png"));
                    imageView.setImage(defaultImg);
                } catch (Exception ignore) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible de charger l'image par défaut.");
                    alert.show();
                }
            }
        });

        // Return button
        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> {
            MainView retourAccueil = new MainView(authService, utilisateur);
            retourAccueil.start(stage);
        });

        VBox layout = new VBox(10, champRecherche, filtre, boutonPhoto, listeResultats, imageView, retourBtn);
        Scene scene = new Scene(layout, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Recherche unifiée");
    }

    /**
     * Highlights matching text in a string using a custom marker.
     *
     * @param texte     the original text
     * @param recherche   the substring to highlight
     * @return the highlighted text with [[RED]] and [[/RED]] markers
     */
    private String surligner(String texte, String recherche) {
        return texte.replaceAll("(?i)(" + Pattern.quote(recherche) + ")", "[[RED]]$1[[/RED]]");
    }

    /**
     * Converts a string with [[RED]]...[[/RED]] markers into a JavaFX Label with colored text.
     *
     * @param ligne the line with optional highlight markers
     * @return a styled Label component
     */
    private Label formatterLigneAvecSurlignage(String ligne) {
        if (!ligne.contains("[[RED]]")) return new Label(ligne);

        int start = ligne.indexOf("[[RED]]");
        int end = ligne.indexOf("[[/RED]]");

        String avant = ligne.substring(0, start);
        String rouge = ligne.substring(start + 7, end);
        String apres = ligne.substring(end + 8);

        TextFlow flow = new TextFlow(
                new Text(avant),
                styledText(rouge, "red"),
                new Text(apres)
        );

        Label label = new Label();
        label.setGraphic(flow);
        return label;
    }

    /**
     * Creates a bold colored Text node for highlighting.
     *
     * @param content the text content
     * @param color   the color to apply
     * @return a styled Text node
     */
    private Text styledText(String content, String color) {
        Text text = new Text(content);
        text.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold;");
        return text;
    }
}
