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

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


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

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox container = new VBox(15);
        scrollPane.setContent(container);

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
        title.setId("title");
        root.getChildren().add(title);

        root.getChildren().add(scrollPane);

        // Bouton retour
        Button retour = new Button("🔙 Retour");
        retour.setOnAction(e -> new MainView(authService, admin).start(stage));
        root.getChildren().add(retour);

        // Scene
        Scene scene = new Scene(root, 800, 800);
        try {
            var cssResource = getClass().getResource("/demandes.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("CSS file not found: /demandes.css");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load CSS stylesheet", ex);
        }

        stage.setScene(scene);
        stage.setTitle("Gestion des demandes");
        stage.show();

        // Affichage initial
        afficherDemandesFiltres(container, "", null);

        // Action bouton filtrer
        boutonFiltrer.setOnAction(e -> {
            String rechercheTexte = champRecherche.getText();
            TypeDemande type = typeComboBox.getValue();
            afficherDemandesFiltres(container, rechercheTexte, type);
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
            blocDemande.getStyleClass().add("bloc-demande");

            Label resume = new Label(demande.getDemandeur().getPrenom() + " demande : " + demande.getType());
            resume.getStyleClass().add("resume-label");

            Button afficherDetails = new Button("Détails");
            afficherDetails.getStyleClass().add("details-button");
            VBox detailsBox = new VBox();
            detailsBox.setVisible(false);
            detailsBox.getStyleClass().add("details-box");

            afficherDetails.setOnAction(e -> detailsBox.setVisible(!detailsBox.isVisible()));
            detailsBox.getChildren().add(getLabel(demande));

            HBox boutons = new HBox(10);
            boutons.setAlignment(Pos.CENTER);

            Button validerBtn = new Button("Valider");
            validerBtn.getStyleClass().add("valider");
            validerBtn.setOnAction(ev -> {
                try {
                    if (admin.getCompte() instanceof Admin) {
                        ((Admin) admin.getCompte()).traiterDemande(demande, true);
                        authService.mettreAJourDemande(demande);
                        afficherDemandesFiltres(container, recherche, filtreType);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la demande", ex);
                }
            });

            Button refuserBtn = new Button("Refuser");
            refuserBtn.getStyleClass().add("refuser");
            refuserBtn.setOnAction(ev -> {
                try {
                    if (admin.getCompte() instanceof Admin) {
                        ((Admin) admin.getCompte()).traiterDemande(demande, false);
                        authService.mettreAJourDemande(demande);
                        authService.supprimerDemandeParID(demande.getId());
                        afficherDemandesFiltres(container, recherche, filtreType);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du refus de la demande", ex);
                }
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
        Label label = new Label(message);
        label.getStyleClass().add("details-content");
        return label;
    }
}
