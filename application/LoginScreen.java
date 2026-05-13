package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;


// login screen...duh
public class LoginScreen {

    private final Stage stage;
    private final AuthService auth = new AuthService();

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {

        //Title
        Text title = new Text("📚 LIBRIS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        title.setFill(Color.web("#2c3e50"));

        Text subtitle = new Text("Library Management System");
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        //Fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        styleField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        styleField(passwordField);

        //Message label
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        //Login Button
        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(300);
        styleButton(loginBtn, "#2c3e50", "#ffffff");

        // login actions
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showMessage(messageLabel, "Please enter username and password.", false);
                return;
            }

            String result = auth.login(username, password);

            if (result.equals("2FA_REQUIRED")) {

                User user = auth.loginUser(username, password);

                if (user == null) {
                    showMessage(messageLabel, "User not found.", false);
                    return;
                }

                // generate OTP
                String otp = String.valueOf(
                        (int)(Math.random() * 900000) + 100000
                );

                boolean sent = EmailService.sendOTP(
                        user.getEmail(),
                        otp
                );

                if (!sent) {
                    showMessage(
                            messageLabel,
                            "Failed to send login OTP.",
                            false
                    );
                    return;
                }

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Email 2FA");
                dialog.setHeaderText(
                        "Enter the OTP sent to your email"
                );
                dialog.setContentText("OTP:");

                java.util.Optional<String> otpResult =
                        dialog.showAndWait();

                if (otpResult.isPresent()) {

                    String enteredOTP =
                            otpResult.get().trim();

                    if (enteredOTP.equals(otp)) {

                        MainDashboard dashboard =
                                new MainDashboard(stage, user);

                        stage.setScene(
                                dashboard.getScene()
                        );

                    } else {
                        showMessage(
                                messageLabel,
                                "Invalid OTP.",
                                false
                        );
                    }
                }

            }
            else if (result.startsWith("Login successful")) {

                User user = auth.loginUser(username, password);

                if (user != null) {
                    MainDashboard dashboard =
                            new MainDashboard(stage, user);

                    stage.setScene(
                            dashboard.getScene()
                    );
                }

            }
            else {
                showMessage(messageLabel, result, false);
            }
        });

        //Register Link
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setFont(Font.font("Arial", 12));
        registerLink.setTextFill(Color.web("#2980b9"));
        registerLink.setBorder(Border.EMPTY);

        // JAVAFX NAVIGATE TO REGISTER (New member, new adventure)
        registerLink.setOnAction(e -> {
            RegisterScreen register = new RegisterScreen(stage);
            stage.setScene(register.getScene());
        });

        //Layout
        VBox form = new VBox(14,
                titleBox,
                new Separator(),
                usernameField,
                passwordField,
                loginBtn,
                messageLabel,
                registerLink
        );
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));
        form.setMaxWidth(380);
        form.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 4);"
        );

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.setPadding(new Insets(60));

        return new Scene(root, 520, 520);
    }

  //I'm too tired to think of aesthetics (typed in 12:24AM)
    private void styleField(TextField field) {
        field.setStyle(
            "-fx-background-radius: 6;" +
            "-fx-border-color: #bdc3c7;" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 8 12;" +
            "-fx-font-size: 13px;"
        );
    }

    private void styleButton(Button btn, String bg, String fg) {
        btn.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + fg + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 0;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #1a252f;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 0;"
        ));
        btn.setOnMouseExited(e -> styleButton(btn, bg, fg));
    }

    private void showMessage(Label label, String msg, boolean success) {
        label.setText(msg);
        label.setTextFill(success ? Color.web("#27ae60") : Color.web("#e74c3c"));
    }
}