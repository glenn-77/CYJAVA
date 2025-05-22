package view;

import dao.DemandeDAO;
import entites.Admin;
import entites.Personne;
import entites.enums.Statut;
import entites.enums.TypeDemande;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.DemandeAdminService.DemandeAdmin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemandesAdminView {

    private static final Logger LOGGER = Logger.getLogger(DemandesAdminView.class.getName());
    private final AuthService authService;
    private final Personne admin;

    public DemandesAdminView(AuthService authService, Personne admin) {
        this.authService = authService;
        this.admin = admin;
    }

    public void start(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Champs de filtre
        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(Pos.CENTER);

        TextField champRecherche = new TextField();
        champRecherche.setPromptText("Rechercher par nom, prénom ou NSS");

        ComboBox<TypeDemande> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().add(null); // Option "tous"
        typeComboBox.getItems().addAll(TypeDemande.values());
        typeComboBox.setPromptText("Filtrer par type");

        Button boutonFiltrer = new Button("🔎 Filtrer");

        filtreBox.getChildren().addAll(champRecherche, typeComboBox, boutonFiltrer);
        root.getChildren().add(filtreBox);

        // Titre
        Label title = new Label("Demandes en attente");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        root.getChildren().add(title);

        // Zone d'affichage des demandes
        VBox listeDemandes = new VBox(15);
        root.getChildren().add(listeDemandes);

        // Bouton retour
        Button retour = new Button("🔙 Retour");
        retour.setOnAction(e -> new MainView(authService, admin).start(stage));
        root.getChildren().add(retour);

        // Scene
        Scene scene = new Scene(root, 800, 800);
        try {
            var cssResource = getClass().getResource("/style.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("CSS file not found: /style.css");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load CSS stylesheet", ex);
        }

        stage.setScene(scene);
        stage.setTitle("Gestion des demandes");
        stage.show();

        // Affichage initial
        afficherDemandesFiltres(listeDemandes, "", null);

        // Action bouton filtrer
        boutonFiltrer.setOnAction(e -> {
            String rechercheTexte = champRecherche.getText();
            TypeDemande type = typeComboBox.getValue();
            afficherDemandesFiltres(listeDemandes, rechercheTexte, type);
        });
    }

    private void afficherDemandesFiltres(VBox container, String recherche, TypeDemande filtreType) {
        container.getChildren().clear();

        Set<DemandeAdmin> resultats = new HashSet<>();
        for (DemandeAdmin d : service.DemandeAdminService.getDemandes()) {
            if (d.getStatut() == Statut.EN_ATTENTE) {
                resultats.add(d);
            }
        }

        if (recherche != null && !recherche.isEmpty()) {
            resultats.removeIf(d ->
                    !d.getDemandeur().getPrenom().toLowerCase().contains(recherche.toLowerCase()) &&
                            !d.getDemandeur().getNom().toLowerCase().contains(recherche.toLowerCase()) &&
                            !d.getDemandeur().getNss().toLowerCase().contains(recherche.toLowerCase())
            );
        }

        if (filtreType != null) {
            resultats.removeIf(d -> d.getType() != filtreType);
        }

        for (DemandeAdmin demande : resultats) {
            VBox blocDemande = new VBox(5);
            blocDemande.setPadding(new Insets(10));
            blocDemande.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-padding: 10;");

            Label resume = new Label(demande.getDemandeur().getPrenom() + " demande : " + demande.getType());

            Button afficherDetails = new Button("Détails");
            VBox detailsBox = new VBox();
            detailsBox.setVisible(false);

            afficherDetails.setOnAction(e -> detailsBox.setVisible(!detailsBox.isVisible()));
            detailsBox.getChildren().add(getLabel(demande));

            HBox boutons = new HBox(10);
            boutons.setAlignment(Pos.CENTER);
            Button validerBtn = new Button("Valider");
            Button refuserBtn = new Button("Refuser");

            validerBtn.setOnAction(ev -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Confirmer la demande ?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            if (admin.getCompte() instanceof Admin) {
                                ((Admin) admin.getCompte()).traiterDemande(demande, true);
                                authService.mettreAJourDemande(demande);
                                afficherDemandesFiltres(container, recherche, filtreType); // refresh
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Error while processing admin request", ex);
                        }
                    }
                });
            });

            refuserBtn.setOnAction(ev -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Refuser la demande ?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            if (admin.getCompte() instanceof Admin) {
                                ((Admin) admin.getCompte()).traiterDemande(demande, false);
                                authService.mettreAJourDemande(demande);
                                afficherDemandesFiltres(container, recherche, filtreType); // refresh
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Error while rejecting admin request", ex);
                        }
                    }
                });
            });

            boutons.getChildren().addAll(validerBtn, refuserBtn);
            blocDemande.getChildren().addAll(resume, afficherDetails, detailsBox, boutons);
            container.getChildren().add(blocDemande);
        }
    }

    @NotNull
    private static Label getLabel(DemandeAdmin demande) {
        String message;
        switch (demande.getType()) {
            case AJOUT_LIEN:
                message = demande.getDemandeur().getPrenom() + " souhaite ajouter un lien '" +
                        demande.getLien() + "' avec " + demande.getCible().getPrenom();
                break;
            case SUPPRESSION_LIEN:
                message = demande.getDemandeur().getPrenom() + " souhaite supprimer le lien avec " +
                        demande.getCible().getPrenom();
                break;
            case MODIFICATION_INFO:
                if (demande.getDemandeur().equals(demande.getCible()))
                    message = demande.getDemandeur().getPrenom() + " souhaite modifier son profil";
                else
                    message = demande.getDemandeur().getPrenom() + " souhaite modifier les infos de " +
                            demande.getCible().getPrenom();
                break;
            case AJOUT_PERSONNE:
                if (demande.getDemandeur().equals(demande.getCible()))
                    message = demande.getDemandeur().getPrenom() + " souhaite s'inscrire sur la plateforme";
                else
                    message = demande.getDemandeur().getPrenom() + " souhaite ajouter " +
                            demande.getCible().getPrenom() + " comme '" + demande.getLien() + "'";
                break;
            default:
                message = "Type de demande inconnu.";
        }
        return new Label(message);
    }
}
