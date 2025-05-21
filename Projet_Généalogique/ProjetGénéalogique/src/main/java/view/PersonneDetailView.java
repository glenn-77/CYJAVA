package view;

import entites.enums.LienParente;
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

public class PersonneDetailView {

    public static void showPopup(Personne personne, Personne utilisateurCourant) {
        Stage popupStage = new Stage();
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        LienParente lien = utilisateurCourant.getLiens().get(personne);
        String lienTexte = lien != null ? lien.name().toLowerCase() : "aucun";

        StringBuilder details = new StringBuilder();

        // Gestion des informations visibles en fonction de la visibilité
        details.append("Détails de la personne :\n")
                .append("Nom : ").append(personne.getNomVisible(utilisateurCourant)).append("\n")
                .append("Prénom : ").append(personne.getPrenomVisible(utilisateurCourant)).append("\n")
                .append("Genre : ").append(personne.estVisiblePar(utilisateurCourant) ? personne.getGenre() : "???").append("\n")
                .append("Date de naissance : ").append(personne.estVisiblePar(utilisateurCourant) ? personne.getDateNaissance() : "???").append("\n")
                .append("Nationalité : ").append(personne.estVisiblePar(utilisateurCourant) ? personne.getNationalite() : "???");

        if (lien != null) {
            details.append("\nLien : ").append(lien.name().toLowerCase());
        }

        // État de vie et inscription
        details.append("\nVivant : ").append(personne.isEstVivant() ? "oui" : "non")
                .append("\nInscrit : ").append(personne.isEstInscrit() ? "oui" : "non");

        Text info = new Text(details.toString());

        if (utilisateurCourant.getLiens().containsKey(personne)) {
            Button boutonSupprimer = new Button("Supprimer");
            boutonSupprimer.setOnAction(e -> {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation de suppression");
                confirmation.setHeaderText("Demande de suppression : " + personne.getPrenomVisible(utilisateurCourant) + " " + personne.getNomVisible(utilisateurCourant));
                confirmation.setContentText("Souhaitez-vous vraiment demander la suppression de cette personne ?");

                confirmation.showAndWait().ifPresent(reponse -> {
                    if (reponse == ButtonType.OK) {
                        String sujet = "Demande de suppression d'une personne";
                        String corps = String.format(
                                "Bonjour Admin,\n\nL'utilisateur %s %s souhaite supprimer %s %s de son arbre généalogique.",
                                personne.getArbre().getProprietaire().getPrenom(),
                                personne.getArbre().getProprietaire().getNom(),
                                personne.getPrenom(),
                                personne.getNom()
                        );
                        MailService.envoyerEmail("admin@exemple.com", sujet, corps);
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
        } else {
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