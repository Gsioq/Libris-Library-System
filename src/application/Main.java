package application;

import javafx.application.Application;
import javafx.stage.Stage;


// Lobby Lmao 
// Run this class to launch all files
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        // stage setup (One window to rule them all)
        primaryStage.setTitle("Libris — Library Management System");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(520);
        primaryStage.setMinHeight(480);

        // LAUNCH LOGIN SCREEN (The front door of the library)
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        primaryStage.setScene(loginScreen.getScene());
        primaryStage.show();
    }

    // Main method (Start here)
    public static void main(String[] args) {
        launch(args);
    }
}