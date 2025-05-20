package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import entites.Personne;
import service.AuthService;
import service.DemandeAdminService;
import service.DemandeAdminService.DemandeAdmin;


public class DemandesAdminView {

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

        Label title = new Label("Demandes en attente");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        root.getChildren().add(title);

        for (DemandeAdmin demande : DemandeAdminService.getDemandes()) {
            Label demandeLabel = new Label(demande.getDemandeur().getPrenom() + " demande un lien '" +
                    demande.getLien() + "' avec " + demande.getCible().getPrenom());

            Button validerBtn = new Button("Valider");
            Button refuserBtn = new Button("Refuser");

            HBox ligne = new HBox(10, demandeLabel, validerBtn, refuserBtn);
            ligne.setAlignment(Pos.CENTER);

            validerBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Confirmer la demande ?", ButtonType.YES, ButtonType.NO);
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                demande.getDemandeur().ajouterLien(demande.getCible(), demande.getLien());
                                DemandeAdminService.supprimerDemande(demande);
                                this.start(stage); // rafraîchir la vue
                            }
                        });
            });

            refuserBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Refuser la demande ?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        DemandeAdminService.supprimerDemande(demande);
                        this.start(stage); // rafraîchir la vue
                    }
                });
            });

            root.getChildren().add(ligne);
        }

        Button retour = new Button("🔙 Retour");
        retour.setOnAction(e -> new MainView(authService, admin).start(stage));
        root.getChildren().add(retour);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Gestion des demandes");
        stage.show();
    }
}
