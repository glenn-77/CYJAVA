package view;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import service.AuthService;
import dao.UserDAO;
import entites.Personne;

import java.util.Set;
import java.util.regex.Pattern;

public class RechercheView {

    private final AuthService authService;
    private final Personne utilisateur;

    public RechercheView(AuthService authService, Personne utilisateur) {
        this.authService = authService;
        this.utilisateur = utilisateur;
    }

    public void start(Stage stage) {
        TextField champRecherche = new TextField();
        champRecherche.setPromptText("Entrez un nom, prénom ou NSS");

        ComboBox<String> filtre = new ComboBox<>();
        filtre.getItems().addAll("Nom", "Prénom", "NSS");
        filtre.setValue("Nom");

        Button boutonRechercher = new Button("Rechercher");
        ListView<Label> listeResultats = new ListView<>();

        champRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            String critere = filtre.getValue();
            listeResultats.getItems().clear();

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
                    listeResultats.getItems().add(label);
                }
            }
        });

        Button retourBtn = new Button("Retour");
        retourBtn.setOnAction(e -> {
            MainView retourAccueil = new MainView(authService, utilisateur);
            retourAccueil.start(stage);
        });

        VBox layout = new VBox(10, champRecherche, filtre, boutonRechercher, listeResultats, retourBtn);
        Scene scene = new Scene(layout, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Recherche unifiée");
    }

    private String surligner(String texte, String recherche) {
        return texte.replaceAll("(?i)(" + Pattern.quote(recherche) + ")", "[[RED]]$1[[/RED]]");
    }

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

    private Text styledText(String content, String color) {
        Text text = new Text(content);
        text.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold;");
        return text;
    }
}
