package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;


// Welcome....Whoever is reading this. I beg you to do the designs. I'm really tired. I did the bare minimum of designs lmao 
public class RegisterScreen {

    private final Stage stage;
    private final AuthService auth = new AuthService();

    public RegisterScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {

        //Title
        Text title = new Text("📚 LIBRIS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        title.setFill(Color.web("#2c3e50"));

        Text subtitle = new Text("Create a New Account");
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        subtitle.setFill(Color.web("#7f8c8d"));

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        //Fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        styleField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleField(passwordField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleField(emailField);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        styleField(phoneField);

        //Role Selector, who are you?
        Label roleLabel = new Label("Select Role:");
        roleLabel.setFont(Font.font("Arial", 12));
        roleLabel.setTextFill(Color.web("#555"));

        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(Role.values());
        roleBox.setValue(Role.READER);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle(
            "-fx-background-radius: 6;" +
            "-fx-border-color: #bdc3c7;" +
            "-fx-border-radius: 6;" +
            "-fx-font-size: 13px;"
        );

        //Message Label
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Arial", 12));

        //Register Button (click me)
        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        styleButton(registerBtn, "#27ae60", "#ffffff");

        // register actions
        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String email    = emailField.getText().trim();
            String phone    = phoneField.getText().trim();
            Role   role     = roleBox.getValue();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showMessage(messageLabel, "Please fill in all fields.", false);
                return;
            }

            String result = auth.register(username, password, email, phone, role);
            boolean success = result.startsWith("Account created");
            showMessage(messageLabel, result, success);

            if (success) {
                // auto verification (idk if this works or not. Might need to test this)
                auth.verify(username);
                showMessage(messageLabel, result + " Account verified. You can now log in!", true);
            }
        });

        // Back to Login Link 
        Hyperlink backLink = new Hyperlink("Already have an account? Log in");
        backLink.setFont(Font.font("Arial", 12));
        backLink.setTextFill(Color.web("#2980b9"));
        backLink.setBorder(Border.EMPTY);

        // navigation, but backwards 
        backLink.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            stage.setScene(login.getScene());
        });

        //Layouts
        VBox form = new VBox(12,
                titleBox,
                new Separator(),
                usernameField,
                passwordField,
                emailField,
                phoneField,
                roleLabel,
                roleBox,
                registerBtn,
                messageLabel,
                backLink
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(36));
        form.setMaxWidth(400);
        form.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 4);"
        );

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.setPadding(new Insets(50));

        return new Scene(root, 520, 620);
    }

    // A E S T H E T I C S
    private void styleField(TextField field) {
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(
            "-fx-background-radius: 6;" +
            "-fx-border-color: #bdc3c7;" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 8 12;" +
            "-fx-font-size: 13px;"
        );
    }

    private void styleButton(Button btn, String bg, String fg) {
        String base =
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + fg + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 0;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base.replace(bg, "#1e8449")));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void showMessage(Label label, String msg, boolean success) {
        label.setText(msg);
        label.setTextFill(success ? Color.web("#27ae60") : Color.web("#e74c3c"));
    }
}