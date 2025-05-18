package view;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Personne;

public class PersonneDetailView {

    public static void showPopup(Personne personne) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Détails de la personne");
        alert.setHeaderText(personne.getPrenom() + " " + personne.getNom());

        StringBuilder contenu = new StringBuilder();
        contenu.append("Genre : ").append(personne.getGenre()).append("\n");
        contenu.append("Date de naissance : ").append(personne.getDateNaissance()).append("\n");
        contenu.append("Nationalité : ").append(personne.getNationalite()).append("\n");
        contenu.append("Carte ID : ").append(personne.getCarteIdentite()).append("\n");

        boolean estInscrit = personne.getCompte() != null;
        contenu.append("Inscrit : ").append(estInscrit ? "Oui" : "Non").append("\n");

        alert.setContentText(contenu.toString());
        alert.showAndWait();
    }
}