import javafx.application.Application;
import javafx.stage.Stage;
import view.AuthService;
import view.LoginView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AuthService authService = new AuthService();
        LoginView loginView = new LoginView(authService);
        loginView.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}