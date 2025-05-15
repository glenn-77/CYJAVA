package test;

import javafx.application.Application;
import javafx.stage.Stage;
import view.AuthService;
import view.MainView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AuthService authService = new AuthService();
        MainView mainView = new MainView(authService);
        mainView.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
