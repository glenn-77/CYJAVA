package view;

import entites.LienParente;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import entites.Personne;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import service.MailService;
import java.util.*;


public class PersonneDetailView {

    public static void showPopup(Personne personne, Personne utilisateurCourant) {
        Stage popupStage = new Stage();
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        LienParente lien = utilisateurCourant.getLiens().get(personne);
        String lienTexte = lien != null ? lien.name().toLowerCase() : "aucun";


        Text info = new Text("Détails de la personne :\n" +
                "Nom : " + personne.getNom() + "\n" +
                "Prénom : " + personne.getPrenom() + "\n" +
                "Genre : " + personne.getGenre() + "\n" +
                "Date de naissance : " + personne.getDateNaissance() + "\n" +
                "Nationalité : " + personne.getNationalite() + "\n" +
                "Lien :" + lienTexte + "\n" +
                "Vivant :" + (personne.isEstVivant() ? "oui" : "non") + "\n" +
                "Inscrit : " + (personne.isEstInscrit() ? "oui" : "non"));

        Set<Personne> personnesDeMonArbre = utilisateurCourant.getEnfants();
        personnesDeMonArbre.add(utilisateurCourant.getMere());
        personnesDeMonArbre.add(utilisateurCourant.getPere());

        if (utilisateurCourant.getLiens().containsKey(personne)) {
            Button boutonSupprimer = new Button("Supprimer");
            boutonSupprimer.setOnAction(e -> {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation de suppression");
                confirmation.setHeaderText("Demande de suppression de " + personne.getPrenom() + " " + personne.getNom());
                confirmation.setContentText("Souhaitez-vous vraiment demander la suppression de cette personne ?");

                confirmation.showAndWait().ifPresent(reponse -> {
                    if (reponse == ButtonType.OK) {
                        String sujet = "📩 Demande de suppression d'une personne";
                        String corps = String.format(
                                "Bonjour Admin,\n\nL'utilisateur %s %s souhaite supprimer %s %s de son arbre généalogique.",
                                personne.getArbre().getProprietaire().getPrenom(),
                                personne.getArbre().getProprietaire().getNom(),
                                personne.getPrenom(),
                                personne.getNom()
                        );
                        MailService.envoyerEmail("diffoglenn007@gmail.com", sujet, corps);
                        Alert confirmationEnvoyee = new Alert(Alert.AlertType.INFORMATION);
                        confirmationEnvoyee.setTitle("Demande envoyée");
                        confirmationEnvoyee.setHeaderText(null);
                        confirmationEnvoyee.setContentText("Votre demande de suppression a été envoyée à l'administrateur.");
                        confirmationEnvoyee.show();
                    }
                });
            });

            Button fermer = new Button("Fermer");
            fermer.setOnAction(e -> popupStage.close());

            layout.getChildren().addAll(info, boutonSupprimer, fermer);
        }
        else {
            Button fermer = new Button("Fermer");
            fermer.setOnAction(e -> popupStage.close());

            layout.getChildren().addAll(info, fermer);
        }

        Scene scene = new Scene(layout, 650, 600);
        popupStage.setTitle("Détails de la personne");
        popupStage.setScene(scene);
        popupStage.show();
    }
}