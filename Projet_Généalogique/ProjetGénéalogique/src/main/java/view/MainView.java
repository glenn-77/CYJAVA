package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.AuthService;

public class MainView {

    private final AuthService authService;

    public MainView(AuthService authService) {
        this.authService = authService;
    }

    public void start(Stage stage) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Se connecter");
        Button registerButton = new Button("S'inscrire");

        loginButton.setOnAction(e -> {
            LoginView loginView = new LoginView(authService);
            loginView.start(stage);
        });

        registerButton.setOnAction(e -> {
            InscriptionView inscriptionView = new InscriptionView(authService);
            inscriptionView.start(stage);
        });

        layout.getChildren().addAll(loginButton, registerButton);
        Scene scene = new Scene(layout, 300, 200);
        stage.setTitle("Bienvenue");
        stage.setScene(scene);
        stage.show();
    }
}
