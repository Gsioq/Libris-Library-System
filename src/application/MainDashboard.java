package application;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

// Lobby (Don't know how many "mains" I need to make. I'm just making them until it works)
public class MainDashboard {

    private final Stage stage;
    private final User currentUser;

    private final AuthService    auth    = new AuthService();
    private final ContentService content = new ContentService();

    public MainDashboard(Stage stage, User currentUser) {
        this.stage       = stage;
        this.currentUser = currentUser;
    }

    public Scene getScene() {

        //Top Bar
        Text appTitle = new Text("📚 LIBRIS");
        appTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        appTitle.setFill(Color.web("#2c3e50"));

        Label userInfo = new Label(
            "Logged in as: " + currentUser.getUsername() +
            "  |  Role: " + currentUser.getRole()
        );
        userInfo.setFont(Font.font("Arial", 13));
        userInfo.setTextFill(Color.web("#555"));

        Button logoutBtn = new Button("Logout");
        styleButton(logoutBtn, "#e74c3c", "#ffffff");

        // Peace out action 
        logoutBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            stage.setScene(login.getScene());
        });

        HBox topBar = new HBox(16, appTitle, userInfo);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        topBar.getChildren().add(logoutBtn);
        topBar.setPadding(new Insets(14, 20, 14, 20));
        topBar.setStyle("-fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        //Tab Pane
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-font-size: 13px;");

        // Always visible tabs
        tabs.getTabs().add(buildHomeTab());
        tabs.getTabs().add(buildSearchTab());
        tabs.getTabs().add(buildReadingProgressTab());
        tabs.getTabs().add(buildBookmarkTab());
        tabs.getTabs().add(buildPaymentTab());
        tabs.getTabs().add(buildSubscriptionTab());
        tabs.getTabs().add(buildAIAssistantTab());

        //Tabs based on your roles 
        if (AuthService.hasAccess(currentUser, Role.ADMIN, Role.LIBRARIAN)) {
            tabs.getTabs().add(buildUploadContentTab());
        }
        if (AuthService.hasAccess(currentUser, Role.ADMIN, Role.CONTENT_MODERATOR)) {
            tabs.getTabs().add(buildModerationTab());
        }
        if (AuthService.hasAccess(currentUser, Role.ADMIN)) {
            tabs.getTabs().add(buildReportTab());
            tabs.getTabs().add(buildLoginActivityTab());
        }

        // roots 
        VBox root = new VBox(topBar, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #f4f6f8;");

        return new Scene(root, 860, 620);
    }

   
    // TAB: HOME (Welcome homie)

    private Tab buildHomeTab() {
        Tab tab = new Tab("🏠 Home");

        Text welcome = new Text("Welcome, " + currentUser.getUsername() + "!");
        welcome.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        welcome.setFill(Color.web("#2c3e50"));

        Text roleText = new Text("Role: " + currentUser.getRole());
        roleText.setFont(Font.font("Georgia", FontPosture.ITALIC, 16));
        roleText.setFill(Color.web("#7f8c8d"));

        Text desc = new Text(
            "Use the tabs above to navigate:\n" +
            "  • Search — find books by keyword\n" +
            "  • Reading Progress — track your pages\n" +
            "  • Bookmarks — save your place\n" +
            "  • Payment & Subscription — manage your plan\n" +
            "  • AI Assistant — ask anything\n" +
            (AuthService.hasAccess(currentUser, Role.ADMIN, Role.LIBRARIAN)
                ? "  • Upload Content — add new books\n" : "") +
            (AuthService.hasAccess(currentUser, Role.ADMIN, Role.CONTENT_MODERATOR)
                ? "  • Moderation — approve or reject books\n" : "") +
            (AuthService.hasAccess(currentUser, Role.ADMIN)
                ? "  • Reports & Login Activity — admin tools\n" : "")
        );
        desc.setFont(Font.font("Arial", 14));
        desc.setFill(Color.web("#444"));
        desc.setLineSpacing(4);

        VBox box = new VBox(18, welcome, roleText, new Separator(), desc);
        box.setPadding(new Insets(40));
        box.setAlignment(Pos.TOP_LEFT);

        tab.setContent(new ScrollPane(box));
        return tab;
    }


    // TAB: SEARCH (Someday, someone's going to look at you...)
 
    private Tab buildSearchTab() {
        Tab tab = new Tab("🔍 Search");

        TextField keywordField = new TextField();
        keywordField.setPromptText("Search by title, author, or genre...");
        styleField(keywordField);

        TextField genreField = new TextField();
        genreField.setPromptText("Filter by genre (exact match)...");
        styleField(genreField);

        Button searchBtn = new Button("Search");
        styleButton(searchBtn, "#2980b9", "#fff");

        Button filterBtn = new Button("Filter by Genre");
        styleButton(filterBtn, "#8e44ad", "#fff");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(320);
        resultArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        // search action (Connects to ContentService.searchContent)
        searchBtn.setOnAction(e -> {
            String keyword = keywordField.getText().trim();
            if (keyword.isEmpty()) {
                resultArea.setText("Please enter a keyword.");
                return;
            }
            // Redirect System.out to TextArea for display
            resultArea.setText("Searching for: \"" + keyword + "\"...\n");
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream old = System.out;
            System.setOut(new java.io.PrintStream(baos));
            content.searchContent(keyword);
            System.out.flush();
            System.setOut(old);
            String output = baos.toString();
            resultArea.setText(output.isEmpty()
                ? "No results found for: \"" + keyword + "\""
                : output);
        });

        // filtration (Connects to ContentService.filterByGenre)
        filterBtn.setOnAction(e -> {
            String genre = genreField.getText().trim();
            if (genre.isEmpty()) {
                resultArea.setText("Please enter a genre.");
                return;
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream old = System.out;
            System.setOut(new java.io.PrintStream(baos));
            content.filterByGenre(genre);
            System.out.flush();
            System.setOut(old);
            String output = baos.toString();
            resultArea.setText(output.isEmpty()
                ? "No books found in genre: \"" + genre + "\""
                : output);
        });

        HBox searchRow = new HBox(10, keywordField, searchBtn);
        HBox.setHgrow(keywordField, Priority.ALWAYS);

        HBox filterRow = new HBox(10, genreField, filterBtn);
        HBox.setHgrow(genreField, Priority.ALWAYS);

        VBox box = new VBox(12, searchRow, filterRow, new Label("Results:"), resultArea);
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // progress read 
    
    private Tab buildReadingProgressTab() {
        Tab tab = new Tab("📖 Reading Progress");

        TextField bookField = new TextField();
        bookField.setPromptText("Book title");
        styleField(bookField);

        TextField totalField = new TextField();
        totalField.setPromptText("Total pages");
        styleField(totalField);

        TextField currentField = new TextField();
        currentField.setPromptText("Current page");
        styleField(currentField);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #27ae60;");

        Label progressLabel = new Label("Progress: 0%");
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button saveBtn = new Button("Save Progress");
        styleButton(saveBtn, "#27ae60", "#fff");

        // reading in progress
        saveBtn.setOnAction(e -> {
            try {
                String title   = bookField.getText().trim();
                int    total   = Integer.parseInt(totalField.getText().trim());
                int    current = Integer.parseInt(currentField.getText().trim());

                ReadingProgress progress = new ReadingProgress(title, total);
                progress.updateProgress(current);

                double pct = progress.getPercentage();
                progressBar.setProgress(pct / 100.0);
                progressLabel.setText(String.format("Progress: %.1f%%", pct));

                ReadingProgressDAO.saveProgress(currentUser.getUsername(), progress);
                showMessage(messageLabel, "Reading progress saved!", true);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Please enter valid page numbers.", false);
            }
        });

        VBox box = new VBox(12,
                sectionLabel("Track Your Reading"),
                bookField, totalField, currentField,
                saveBtn, progressBar, progressLabel, messageLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // TAB: Bookmark (I hope my brain can still remember how I made this amalgamation, once I'm awake) 
 
    private Tab buildBookmarkTab() {
        Tab tab = new Tab("Bookmarks");

        TextField bookField = new TextField();
        bookField.setPromptText("Book title");
        styleField(bookField);

        TextField pageField = new TextField();
        pageField.setPromptText("Page number");
        styleField(pageField);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button saveBtn = new Button("Save Bookmark");
        styleButton(saveBtn, "#e67e22", "#fff");

        // actions of bookmark  (Connects to BookmarkDAO.saveBookmark)
        saveBtn.setOnAction(e -> {
            try {
                String title = bookField.getText().trim();
                int    page  = Integer.parseInt(pageField.getText().trim());

                if (title.isEmpty()) {
                    showMessage(messageLabel, "Please enter a book title.", false);
                    return;
                }

                Bookmark bookmark = new Bookmark(title, page);
                BookmarkDAO.saveBookmark(currentUser.getUsername(), bookmark);
                showMessage(messageLabel, "Bookmark saved: \"" + title + "\" — page " + page, true);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Please enter a valid page number.", false);
            }
        });

        VBox box = new VBox(12,
                sectionLabel("Save a Bookmark"),
                bookField, pageField, saveBtn, messageLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // Libre me if I did a good job 
    private Tab buildPaymentTab() {
        Tab tab = new Tab("Payment");

        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("GCash", "PayMaya", "Credit Card", "Bank Transfer");
        methodBox.setValue("GCash");
        methodBox.setMaxWidth(Double.MAX_VALUE);
        methodBox.setStyle("-fx-font-size: 13px;");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (PHP)");
        styleField(amountField);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button payBtn = new Button("Process Payment");
        styleButton(payBtn, "#2c3e50", "#fff");

        // payment on the move 
        payBtn.setOnAction(e -> {
            try {
                String method = methodBox.getValue();
                double amount = Double.parseDouble(amountField.getText().trim());

                Payment payment = new Payment(method, amount);
                payment.processPayment();
                PaymentDAO.savePayment(currentUser.getUsername(), payment);

                showMessage(messageLabel,
                    payment.paid
                        ? "Payment of PHP " + amount + " via " + method + " processed!"
                        : "Payment failed — amount must be greater than 0.",
                    payment.paid);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Please enter a valid amount.", false);
            }
        });

        VBox box = new VBox(12,
                sectionLabel("Make a Payment"),
                new Label("Payment Method:"), methodBox,
                amountField, payBtn, messageLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // Thank you surfshark for giving me an expensive subscription 
    private Tab buildSubscriptionTab() {
        Tab tab = new Tab("⭐ Subscription");

        ComboBox<String> planBox = new ComboBox<>();
        planBox.getItems().addAll("Basic Monthly", "Premium Monthly", "Premium Annual");
        planBox.setValue("Basic Monthly");
        planBox.setMaxWidth(Double.MAX_VALUE);
        planBox.setStyle("-fx-font-size: 13px;");

        TextField priceField = new TextField();
        priceField.setPromptText("Price (PHP)");
        styleField(priceField);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button activateBtn = new Button("Activate Subscription");
        styleButton(activateBtn, "#8e44ad", "#fff");

        Button cancelBtn = new Button("Cancel Subscription");
        styleButton(cancelBtn, "#e74c3c", "#fff");

        // Did you pay? If customer paid..Then access to premium 
        activateBtn.setOnAction(e -> {
            try {
                String plan  = planBox.getValue();
                double price = Double.parseDouble(priceField.getText().trim());

                Subscription sub = new Subscription(plan, price);
                sub.activate();
                SubscriptionDAO.saveSubscription(currentUser.getUsername(), sub);
                showMessage(messageLabel, "Subscribed to: " + plan + " (PHP " + price + ")", true);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Please enter a valid price.", false);
            }
        });

        // I feel you on this cancel 
        cancelBtn.setOnAction(e -> {
            try {
                String plan  = planBox.getValue();
                double price = Double.parseDouble(priceField.getText().trim());

                Subscription sub = new Subscription(plan, price);
                sub.cancel();
                SubscriptionDAO.saveSubscription(currentUser.getUsername(), sub);
                showMessage(messageLabel, "Subscription cancelled: " + plan, false);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Please enter a valid price.", false);
            }
        });

        HBox btnRow = new HBox(10, activateBtn, cancelBtn);

        VBox box = new VBox(12,
                sectionLabel("Manage Subscription"),
                new Label("Plan:"), planBox,
                priceField, btnRow, messageLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

   // Who suggested AI integration again? 
    private Tab buildAIAssistantTab() {
        Tab tab = new Tab("🤖 AI Assistant");

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(360);
        chatArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        chatArea.setText("Ask the AI anything about books, reading, or the library!\n\n");

        TextField questionField = new TextField();
        questionField.setPromptText("Type your question...");
        styleField(questionField);

        Button askBtn = new Button("Ask AI");
        styleButton(askBtn, "#16a085", "#fff");

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", 11));
        statusLabel.setTextFill(Color.web("#888"));

        // Ai in action 
        askBtn.setOnAction(e -> {
            String question = questionField.getText().trim();
            if (question.isEmpty()) return;

            questionField.clear();
            askBtn.setDisable(true);
            statusLabel.setText("Thinking...");
            chatArea.appendText("You: " + question + "\n");

         
            Task<String> aiTask = new Task<>() {
                @Override
                protected String call() {
                    return AIAssistant.askAIForUI(question);
                }
            };

            aiTask.setOnSucceeded(ev -> {
                chatArea.appendText("AI: " + aiTask.getValue() + "\n\n");
                askBtn.setDisable(false);
                statusLabel.setText("");
            });

            aiTask.setOnFailed(ev -> {
                chatArea.appendText("AI: Sorry, something went wrong.\n\n");
                askBtn.setDisable(false);
                statusLabel.setText("");
            });

            new Thread(aiTask).start();
        });

        // press enter to send 
        questionField.setOnAction(e -> askBtn.fire());

        HBox inputRow = new HBox(10, questionField, askBtn);
        HBox.setHgrow(questionField, Priority.ALWAYS);

        VBox box = new VBox(12,
                sectionLabel("AI Assistant (Gemini)"),
                chatArea, inputRow, statusLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // Uploading part 
    private Tab buildUploadContentTab() {
        Tab tab = new Tab("📤 Upload Content");

        TextField titleField = new TextField();
        titleField.setPromptText("Book title");
        styleField(titleField);

        TextField authorField = new TextField();
        authorField.setPromptText("Author");
        styleField(authorField);

        TextField genreField = new TextField();
        genreField.setPromptText("Genre");
        styleField(genreField);

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(4);
        descArea.setWrapText(true);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button uploadBtn = new Button("Upload Book");
        styleButton(uploadBtn, "#2980b9", "#fff");

        // Upload in action 
        uploadBtn.setOnAction(e -> {
            String title  = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre  = genreField.getText().trim();
            String desc   = descArea.getText().trim();

            if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || desc.isEmpty()) {
                showMessage(messageLabel, "Please fill in all fields.", false);
                return;
            }

            String result = content.uploadContent(currentUser, title, author, genre, desc);
            boolean success = result.startsWith("Content uploaded");
            showMessage(messageLabel, result, success);

            if (success) {
                titleField.clear();
                authorField.clear();
                genreField.clear();
                descArea.clear();
            }
        });

        VBox box = new VBox(12,
                sectionLabel("Upload a Book"),
                titleField, authorField, genreField,
                new Label("Description:"), descArea,
                uploadBtn, messageLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(new ScrollPane(box));
        return tab;
    }

    // Moderation 
    private Tab buildModerationTab() {
        Tab tab = new Tab("🛡 Moderation");

        TextField bookIdField = new TextField();
        bookIdField.setPromptText("Book ID");
        styleField(bookIdField);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("APPROVED", "REJECTED", "PENDING");
        statusBox.setValue("APPROVED");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle("-fx-font-size: 13px;");

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button moderateBtn = new Button("Apply Status");
        styleButton(moderateBtn, "#c0392b", "#fff");

        // JAVAFX MODERATION ACTION (Connects to ContentService.moderateContent)
        moderateBtn.setOnAction(e -> {
            try {
                int    bookId = Integer.parseInt(bookIdField.getText().trim());
                String status = statusBox.getValue();

                String result = content.moderateContent(currentUser, bookId, status);
                boolean success = result.startsWith("Content moderated");
                showMessage(messageLabel, result, success);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Please enter a valid Book ID.", false);
            }
        });

        VBox box = new VBox(12,
                sectionLabel("Moderate Content"),
                bookIdField,
                new Label("Set Status:"), statusBox,
                moderateBtn, messageLabel
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // Admin 
    private Tab buildReportTab() {
        Tab tab = new Tab("📊 Reports");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username to view report");
        styleField(usernameField);

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        reportArea.setPrefHeight(320);
        reportArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        Button viewBtn = new Button("View Report");
        styleButton(viewBtn, "#2c3e50", "#fff");

        // reports in action 
        viewBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                reportArea.setText("Please enter a username.");
                return;
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream old = System.out;
            System.setOut(new java.io.PrintStream(baos));
            ReportDAO.viewUserActivity(username);
            System.out.flush();
            System.setOut(old);
            String output = baos.toString();
            reportArea.setText(output.isEmpty() ? "No activity found for: " + username : output);
        });

        VBox box = new VBox(12,
                sectionLabel("User Activity Report"),
                usernameField, viewBtn,
                new Label("Report Output:"), reportArea
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // I know what you're doing ahh
    private Tab buildLoginActivityTab() {
        Tab tab = new Tab("🔐 Login Activity");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        styleField(usernameField);

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(320);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        Button viewBtn = new Button("View Login Activity");
        styleButton(viewBtn, "#7f8c8d", "#fff");

        // Login actions
        viewBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                logArea.setText("Please enter a username.");
                return;
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream old = System.out;
            System.setOut(new java.io.PrintStream(baos));
            auth.viewLoginActivity(username);
            System.out.flush();
            System.setOut(old);
            String output = baos.toString();
            logArea.setText(output.isEmpty() ? "No login activity found for: " + username : output);
        });

        VBox box = new VBox(12,
                sectionLabel("Login Activity Log"),
                usernameField, viewBtn,
                new Label("Activity Log:"), logArea
        );
        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }

    // A E S T H E T I C S God help me (typed in 2:38AM)
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
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;";
        btn.setStyle(base);
    }

    private void showMessage(Label label, String msg, boolean success) {
        label.setText(msg);
        label.setTextFill(success ? Color.web("#27ae60") : Color.web("#e74c3c"));
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web("#2c3e50"));
        return lbl;
    }
}