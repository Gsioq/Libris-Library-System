package application;

import javafx.application.Application;
import javafx.stage.Stage;


// Lobby Lmao 
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        // stage setup
        primaryStage.setTitle("Libris — Library Management System");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(520);
        primaryStage.setMinHeight(480);

        LoginScreen loginScreen = new LoginScreen(primaryStage);
        primaryStage.setScene(loginScreen.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}