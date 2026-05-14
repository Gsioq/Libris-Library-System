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
import java.io.File;
import javafx.stage.FileChooser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import javafx.scene.control.Alert;
import java.sql.*;
import database.DBConnection;


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
        tabs.getTabs().add(buildDiscussionTab());
        tabs.getTabs().add(buildReviewTab());
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
            tabs.getTabs().add(buildManageRolesTab());
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

        Bookmark latest = BookmarkDAO.getLatestBookmark(currentUser.getUsername());

        if (latest != null) {
            Label bookmarkInfo = new Label(
                "Continue Reading: " +
                latest.getBookTitle() +
                " (Page " +
                latest.getPageNumber() + ")"
            );

            Button continueBtn = new Button("Continue Reading");
            styleButton(continueBtn, "#27ae60", "#fff");

            continueBtn.setOnAction(e -> {
                try {
                    String title = latest.getBookTitle();
                    int page = latest.getPageNumber();

                    String filePath = content.getBookFilePath(title);

                    if (filePath == null) {
                        Alert alert = new Alert(
                            Alert.AlertType.ERROR,
                            "Book file not found."
                        );
                        alert.showAndWait();
                        return;
                    }

                    File file = new File(filePath);

                    PDDocument document = Loader.loadPDF(file);

                    PDFTextStripper stripper = new PDFTextStripper();

                    int totalPages = document.getNumberOfPages();

                    final int[] currentPage = {page};

                    TextArea readerArea = new TextArea();
                    readerArea.setWrapText(true);
                    readerArea.setEditable(false);
                    readerArea.setPrefHeight(500);

                    readerArea.setStyle(
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Georgia';" +
                        "-fx-padding: 20;"
                    );

                    Runnable loadPage = () -> {
                        try {
                            stripper.setStartPage(currentPage[0]);
                            stripper.setEndPage(currentPage[0]);

                            String pageText = stripper.getText(document);

                            readerArea.setText(
                                "Page " + currentPage[0] + " of " + totalPages + "\n\n" +
                                pageText
                            );
                            
                            try {
                                ReadingProgress progress = new ReadingProgress(
                                        title,
                                        totalPages
                                );

                                progress.updateProgress(currentPage[0]);

                                ReadingProgressDAO.saveProgress(
                                        currentUser.getUsername(),
                                        progress
                                );

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    };

                    loadPage.run();

                    Button prevBtn = new Button("Previous Page");
                    styleButton(prevBtn, "#3498db", "#fff");

                    Button nextBtn = new Button("Next Page");
                    styleButton(nextBtn, "#27ae60", "#fff");

                    Button closeBtn = new Button("Close Reader");
                    styleButton(closeBtn, "#e74c3c", "#fff");
                    
                    Button bookmarkBtn = new Button("Bookmark Current Page");
                    styleButton(bookmarkBtn, "#f39c12", "#fff");

                    Label bookmarkLabel = new Label();

                    prevBtn.setOnAction(event -> {
                        if (currentPage[0] > 1) {
                            currentPage[0]--;
                            loadPage.run();
                        }
                    });

                    nextBtn.setOnAction(event -> {
                        if (currentPage[0] < totalPages) {
                            currentPage[0]++;
                            loadPage.run();
                        }
                    });
                    
                    bookmarkBtn.setOnAction(event -> {
                        try {
                            Bookmark bookmark = new Bookmark(
                                    title,
                                    currentPage[0]
                            );

                            BookmarkDAO.saveBookmark(
                                    currentUser.getUsername(),
                                    bookmark
                            );

                            bookmarkLabel.setText(
                                    "Bookmarked page " + currentPage[0]
                            );

                            bookmarkLabel.setTextFill(Color.GREEN);

                            // refresh dashboard home page bookmark display
                            stage.setScene(new MainDashboard(stage, currentUser).getScene());

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            bookmarkLabel.setText("Failed to save bookmark.");
                            bookmarkLabel.setTextFill(Color.RED);
                        }
                    });

                    HBox navButtons = new HBox(10, prevBtn, nextBtn);
                    navButtons.setAlignment(Pos.CENTER);

                    double percentage = ((double) currentPage[0] / totalPages) * 100;

                    Label progressLabel = new Label(
                            String.format("Progress: %.1f%%", percentage)
                    );

                    progressLabel.setFont(
                            Font.font("Arial", FontWeight.BOLD, 14)
                    );

                    VBox sidePanel = new VBox(
                            20,
                            progressLabel,
                            bookmarkBtn,
                            bookmarkLabel,
                            closeBtn
                    );

                    sidePanel.setAlignment(Pos.CENTER);
                    sidePanel.setPrefWidth(220);

                    HBox readingSection = new HBox(
                            30,
                            readerArea,
                            sidePanel
                    );

                    readingSection.setAlignment(Pos.CENTER);

                    HBox.setHgrow(readerArea, Priority.ALWAYS);
                    readerArea.setMaxWidth(700);

                    VBox readerLayout = new VBox(
                            15,
                            new Label("📖 Continuing: " + title),
                            readingSection,
                            navButtons
                    );

                    readerLayout.setPadding(new Insets(20));

                    Tab readerTab = new Tab("📖 " + title);
                    readerTab.setClosable(true);
                    readerTab.setContent(readerLayout);

                    TabPane parentTabs = (TabPane) tab.getTabPane();
                    parentTabs.getTabs().add(readerTab);
                    parentTabs.getSelectionModel().select(readerTab);

                    closeBtn.setOnAction(event -> {
                        parentTabs.getTabs().remove(readerTab);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            box.getChildren().addAll(
                new Separator(),
                bookmarkInfo,
                continueBtn
            );
        }
        
        if (!currentUser.isTwoFactorEnabled()) {

            Label twoFALabel = new Label(
                "Add extra security to your account"
            );

            Button enable2FABtn = new Button("Enable Email 2FA");
            styleButton(enable2FABtn, "#8e44ad", "#fff");

            enable2FABtn.setOnAction(e -> {
                UserDAO.enableTwoFactor(
                    currentUser.getUsername()
                );

                showAlert(
                    "Success",
                    "Email 2FA has been enabled."
                );

                stage.setScene(
                    new MainDashboard(stage, currentUser).getScene()
                );
            });

            box.getChildren().addAll(
                new Separator(),
                twoFALabel,
                enable2FABtn
            );
        }

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

        Button readBtn = new Button("Read Book");
        styleButton(readBtn, "#27ae60", "#fff");
        
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
        
        readBtn.setOnAction(e -> {
            String title = keywordField.getText().trim();

            if (title.isEmpty()) {
                resultArea.setText("Enter a book title first.");
                return;
            }

            try {
                String filePath = content.getBookFilePath(title);

                if (filePath == null) {
                    resultArea.setText("No PDF found for this book.");
                    return;
                }

                File file = new File(filePath);

                PDDocument document = Loader.loadPDF(file);
                PDFTextStripper stripper = new PDFTextStripper();

                int totalPages = document.getNumberOfPages();

                final int[] currentPage = {1};

                TextArea readerArea = new TextArea();
                readerArea.setWrapText(true);
                readerArea.setEditable(false);
                readerArea.setPrefHeight(500);

                readerArea.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-font-family: 'Georgia';" +
                    "-fx-padding: 20;"
                );
                
                double percentage = ((double) currentPage[0] / totalPages) * 100;

                Label progressLabel = new Label(
                    String.format("Progress: %.1f%%", percentage)
                );

                	progressLabel.setFont(
                	    Font.font("Arial", FontWeight.BOLD, 14)
                	);


                Runnable loadPage = () -> {
                    try {
                        stripper.setStartPage(currentPage[0]);
                        stripper.setEndPage(currentPage[0]);

                        String pageText = stripper.getText(document);

                        readerArea.setText(
                            "Page " + currentPage[0] + " of " + totalPages + "\n\n" +
                            pageText
                        );

                        double updatedPercentage =
                                ((double) currentPage[0] / totalPages) * 100;

                        progressLabel.setText(
                                String.format("Progress: %.1f%%", updatedPercentage)
                        );

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                };

                loadPage.run();

                Button prevBtn = new Button("Previous Page");
                styleButton(prevBtn, "#3498db", "#fff");

                Button nextBtn = new Button("Next Page");
                styleButton(nextBtn, "#27ae60", "#fff");

                Button closeBtn = new Button("Close Reader");
                styleButton(closeBtn, "#e74c3c", "#fff");

                Button bookmarkBtn = new Button("Bookmark Current Page");
                styleButton(bookmarkBtn, "#f39c12", "#fff");

                Label bookmarkLabel = new Label();

                prevBtn.setOnAction(event -> {
                    if (currentPage[0] > 1) {
                        currentPage[0]--;
                        loadPage.run();
                    }
                });

                nextBtn.setOnAction(event -> {
                    if (currentPage[0] < totalPages) {
                        currentPage[0]++;
                        loadPage.run();
                    }
                });

                bookmarkBtn.setOnAction(event -> {
                    try {
                        Bookmark bookmark = new Bookmark(
                                title,
                                currentPage[0]
                        );

                        BookmarkDAO.saveBookmark(
                                currentUser.getUsername(),
                                bookmark
                        );

                        bookmarkLabel.setText(
                                "Bookmarked page " + currentPage[0]
                        );

                        bookmarkLabel.setTextFill(Color.GREEN);
                        
                        stage.setScene(new MainDashboard(stage, currentUser).getScene());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        bookmarkLabel.setText("Failed to save bookmark.");
                        bookmarkLabel.setTextFill(Color.RED);
                    }
                });

                HBox navButtons = new HBox(10, prevBtn, nextBtn);
                navButtons.setAlignment(Pos.CENTER);

                VBox sidePanel = new VBox(
                        20,
                        progressLabel,
                        bookmarkBtn,
                        bookmarkLabel,
                        closeBtn
                );

                sidePanel.setAlignment(Pos.TOP_CENTER);
                sidePanel.setPrefWidth(220);

                HBox readingSection = new HBox(
                        30,
                        readerArea,
                        sidePanel
                );

                readingSection.setAlignment(Pos.CENTER);

                HBox.setHgrow(readerArea, Priority.ALWAYS);
                readerArea.setMaxWidth(700);

                VBox readerLayout = new VBox(
                        15,
                        new Label("📖 Now Reading: " + title),
                        readingSection,
                        navButtons
                );

                readerLayout.setPadding(new Insets(20));

                Tab readerTab = new Tab("📖 " + title);
                readerTab.setClosable(true);
                readerTab.setContent(readerLayout);

                // adds new reading tab dynamically
                TabPane parentTabs = (TabPane) tab.getTabPane();
                parentTabs.getTabs().add(readerTab);
                parentTabs.getSelectionModel().select(readerTab);

                closeBtn.setOnAction(event -> {
                    parentTabs.getTabs().remove(readerTab);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                resultArea.setText("Failed to open PDF.");
            }
        });

        HBox searchRow = new HBox(10, keywordField, searchBtn, readBtn);        HBox.setHgrow(keywordField, Priority.ALWAYS);

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

    private Tab buildDiscussionTab() {
        Tab tab = new Tab("💬 Discussions");

        TextField titleField = new TextField();
        titleField.setPromptText("Discussion title");
        styleField(titleField);

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Start a discussion...");
        contentArea.setWrapText(true);
        contentArea.setPrefHeight(150);

        TextArea feedArea = new TextArea();
        feedArea.setEditable(false);
        feedArea.setWrapText(true);
        feedArea.setPrefHeight(250);

        Button postBtn = new Button("Post Discussion");
        styleButton(postBtn, "#3498db", "#fff");
        
        Button loadDiscussionsBtn = new Button("Load Discussions");
        styleButton(loadDiscussionsBtn, "#16a085", "#fff");

        Label messageLabel = new Label();

        // Load ALL approved discussions
        refreshDiscussionFeed(feedArea);

        postBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if(title.isEmpty() || content.isEmpty()){
                messageLabel.setText("Fill in all fields.");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            Discussion discussion = new Discussion(
                    currentUser.getUsername(),
                    title,
                    content
            );

            DiscussionDAO.createDiscussion(discussion);

            messageLabel.setText("Discussion posted.");
            messageLabel.setTextFill(Color.GREEN);

            titleField.clear();
            contentArea.clear();

            refreshDiscussionFeed(feedArea);
        });
        
        loadDiscussionsBtn.setOnAction(e -> {
            refreshDiscussionFeed(feedArea);
        });

        VBox box = new VBox(
                12,
                titleField,
                contentArea,
                postBtn,
                loadDiscussionsBtn,
                messageLabel,
                new Label("Public Discussions:"),
                feedArea
        );

        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }
    
    private Tab buildReviewTab() {
        Tab tab = new Tab("⭐ Reviews");

        TextField bookField = new TextField();
        bookField.setPromptText("Book title");
        styleField(bookField);

        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(1,2,3,4,5);
        ratingBox.setValue(5);

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your review...");
        reviewArea.setWrapText(true);

        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);

        Button postBtn = new Button("Post Review");
        styleButton(postBtn, "#f39c12", "#fff");
        
        Button loadReviewsBtn = new Button("Load Reviews");
        styleButton(loadReviewsBtn, "#3498db", "#fff");

        Label messageLabel = new Label();

        postBtn.setOnAction(e -> {
            String book = bookField.getText().trim();
            String reviewText = reviewArea.getText().trim();

            if(book.isEmpty() || reviewText.isEmpty()){
                messageLabel.setText("Complete all fields.");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            Review review = new Review(
                    currentUser.getUsername(),
                    book,
                    ratingBox.getValue(),
                    reviewText
            );

            ReviewDAO.createReview(review);

            messageLabel.setText("Review posted.");
            messageLabel.setTextFill(Color.GREEN);

            reviewArea.clear();

            refreshReviewFeed(displayArea, book);
        });
        
        loadReviewsBtn.setOnAction(e -> {
            String book = bookField.getText().trim();

            if(book.isEmpty()){
                messageLabel.setText("Enter a book title first.");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            refreshReviewFeed(displayArea, book);
        });

        bookField.setOnAction(e -> {
            refreshReviewFeed(displayArea, bookField.getText().trim());
        });

        VBox box = new VBox(
                12,
                bookField,
                loadReviewsBtn,   
                ratingBox,
                reviewArea,
                postBtn,
                messageLabel,
                new Label("Book Reviews:"),
                displayArea
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

        File[] selectedFile = new File[1];

        Label fileLabel = new Label("No PDF selected");
        fileLabel.setFont(Font.font("Arial", 12));

        Button chooseFileBtn = new Button("Choose PDF");
        styleButton(chooseFileBtn, "#8e44ad", "#fff");
        
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

            if (selectedFile[0] == null) {
                showMessage(messageLabel, "Please select a PDF file.", false);
                return;
            }

            String result = content.uploadContent(
                    currentUser,
                    title,
                    author,
                    genre,
                    desc,
                    selectedFile[0].getAbsolutePath()
            );
            
            boolean success = result.startsWith("Content uploaded");
            showMessage(messageLabel, result, success);

            if (success) {
                titleField.clear();
                authorField.clear();
                genreField.clear();
                descArea.clear();
            }
        });
        
        chooseFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();

            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText("Selected: " + file.getName());
            }
        });

        VBox box = new VBox(12,
                sectionLabel("Upload a Book"),
                titleField,
                authorField,
                genreField,
                new Label("Description:"),
                descArea,
                chooseFileBtn,
                fileLabel,
                uploadBtn,
                messageLabel
        );
        
        box.setPadding(new Insets(24));

        tab.setContent(new ScrollPane(box));
        return tab;
    }

    // Moderation 
    private Tab buildModerationTab() {
        Tab tab = new Tab("🛡 Moderation");

        TextArea notificationArea = new TextArea();
        notificationArea.setEditable(false);
        notificationArea.setPrefHeight(100);

        TextArea contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setPrefHeight(300);

        TextField idField = new TextField();
        idField.setPromptText("Enter Content ID");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("DISCUSSION", "REVIEW");
        typeBox.setValue("DISCUSSION");

        Button loadBtn = new Button("Load Pending Content");
        styleButton(loadBtn, "#3498db", "#fff");

        Button approveBtn = new Button("Approve");
        styleButton(approveBtn, "#27ae60", "#fff");

        Button flagBtn = new Button("Flag");
        styleButton(flagBtn, "#e74c3c", "#fff");

        loadBtn.setOnAction(e -> {
            String notifications =
                    ManualModerationDAO.getNotifications();

            String discussions =
                    ManualModerationDAO.getPendingDiscussions();

            String reviews =
                    ManualModerationDAO.getPendingReviews();

            notificationArea.setText(notifications);

            contentArea.setText(
                    "PENDING DISCUSSIONS:\n\n" +
                    discussions +
                    "\n\nPENDING REVIEWS:\n\n" +
                    reviews
            );
        });

        approveBtn.setOnAction(e -> {
            try {
                String[] ids = idField.getText().split(",");

                for(String idText : ids){
                    int id = Integer.parseInt(idText.trim());

                    boolean success;

                    if(typeBox.getValue().equals("DISCUSSION")) {
                        success = ManualModerationDAO.approveDiscussion(id);
                    } else {
                        success = ManualModerationDAO.approveReview(id);
                    }

                    if(!success){
                        contentArea.setText(
                            "Invalid ID or content was already moderated."
                        );
                        return;
                    }
                }

                String discussions = ManualModerationDAO.getPendingDiscussions();
                String reviews = ManualModerationDAO.getPendingReviews();

                contentArea.setText(
                    "PENDING DISCUSSIONS:\n\n" +
                    discussions +
                    "\n\nPENDING REVIEWS:\n\n" +
                    reviews
                );

                idField.clear();

            } catch(Exception ex){
                contentArea.setText("Please enter valid numeric IDs.");
            }
        });

        flagBtn.setOnAction(e -> {
            try {
                String[] ids = idField.getText().split(",");

                for(String idText : ids){
                    int id = Integer.parseInt(idText.trim());

                    if(typeBox.getValue().equals("DISCUSSION")) {
                        ManualModerationDAO.flagDiscussion(id);
                    } else {
                        ManualModerationDAO.flagReview(id);
                    }
                }

                contentArea.setText("Selected content flagged.");

                String discussions = ManualModerationDAO.getPendingDiscussions();
                String reviews = ManualModerationDAO.getPendingReviews();

                contentArea.setText(
                    "PENDING DISCUSSIONS:\n\n" +
                    discussions +
                    "\n\nPENDING REVIEWS:\n\n" +
                    reviews
                );

            } catch(Exception ex){
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(
                10,
                new Label("Moderator Notifications"),
                notificationArea,
                loadBtn,
                contentArea,
                typeBox,
                idField,
                approveBtn,
                flagBtn
        );

        layout.setPadding(new Insets(20));

        tab.setContent(layout);

        return tab;
    }

    // Admin 
    private Tab buildReportTab() {
        Tab tab = new Tab("📊 Reports");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username to ban/ unban");
        styleField(usernameField);

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        reportArea.setPrefHeight(320);
        reportArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        
        Button flaggedBtn = new Button("View Flagged Content");
        styleButton(flaggedBtn, "#e67e22", "#fff");
        
        Button banBtn = new Button("Ban User");
        styleButton(banBtn, "#e74c3c", "#fff");

        Button unbanBtn = new Button("Unban User");
        styleButton(unbanBtn, "#27ae60", "#fff");

        // reports in action 
        banBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();

            if(username.isEmpty()){
                reportArea.setText("Enter a username.");
                return;
            }

            boolean success = UserDAO.banUser(username);

            if(success){
                reportArea.setText(username + " has been banned.");
            } else {
                reportArea.setText("User not found.");
            }
        });
        
        unbanBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();

            if(username.isEmpty()){
                reportArea.setText("Enter a username.");
                return;
            }

            boolean success = UserDAO.unbanUser(username);

            if(success){
                reportArea.setText(username + " has been unbanned.");
            } else {
                reportArea.setText("User not found.");
            }
        });
        
        flaggedBtn.setOnAction(e -> {
            try(Connection conn = DBConnection.connect()) {

                String reviewSql = """
                    SELECT username, book_title, review_text
                    FROM reviews
                    WHERE status='FLAGGED'
                """;

                String discussionSql = """
                    SELECT username, title, content
                    FROM discussions
                    WHERE status='FLAGGED'
                """;

                StringBuilder output = new StringBuilder();

                // Flagged reviews
                PreparedStatement reviewStmt =
                        conn.prepareStatement(reviewSql);

                ResultSet reviewRs =
                        reviewStmt.executeQuery();

                output.append("FLAGGED REVIEWS:\n\n");

                while(reviewRs.next()){
                    output.append("User: ")
                          .append(reviewRs.getString("username"))
                          .append("\nBook: ")
                          .append(reviewRs.getString("book_title"))
                          .append("\nReview: ")
                          .append(reviewRs.getString("review_text"))
                          .append("\n-------------------\n");
                }

                // Flagged discussions
                PreparedStatement discussionStmt =
                        conn.prepareStatement(discussionSql);

                ResultSet discussionRs =
                        discussionStmt.executeQuery();

                output.append("\nFLAGGED DISCUSSIONS:\n\n");

                while(discussionRs.next()){
                    output.append("User: ")
                          .append(discussionRs.getString("username"))
                          .append("\nTitle: ")
                          .append(discussionRs.getString("title"))
                          .append("\nContent: ")
                          .append(discussionRs.getString("content"))
                          .append("\n-------------------\n");
                }

                if(output.toString().equals(
                        "FLAGGED REVIEWS:\n\n\nFLAGGED DISCUSSIONS:\n\n"
                )){
                    reportArea.setText(
                        "No flagged content found."
                    );
                } else {
                    reportArea.setText(output.toString());
                }

            } catch(Exception ex){
                ex.printStackTrace();
            }
        });

        VBox box = new VBox(12,
                sectionLabel("User Activity Report"),
                usernameField,
                banBtn,
                unbanBtn,
                flaggedBtn,
                new Label("Report Output:"),
                reportArea
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
    
    private Tab buildManageRolesTab() {
        Tab tab = new Tab("👥 Manage Roles");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(
                "LIBRARIAN",
                "CONTENT_MODERATOR",
                "ADMIN"
        );

        roleBox.setValue("LIBRARIAN");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        Button sendInviteBtn = new Button("Send Invite");
        styleButton(sendInviteBtn, "#8e44ad", "#fff");

        sendInviteBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String role = roleBox.getValue();

            String code = "INV-" + System.currentTimeMillis();

            RoleInviteDAO.createInvite(
                    email,
                    role,
                    code
            );
            
            EmailService.sendRoleInviteEmail(
                    email,
                    role,
                    code
            );

            outputArea.setText(
            	    "Invitation email sent successfully."
            	);
        });

        VBox box = new VBox(
                12,
                emailField,
                roleBox,
                sendInviteBtn,
                outputArea
        );

        box.setPadding(new Insets(24));

        tab.setContent(box);
        return tab;
    }
    
    private void refreshDiscussionFeed(TextArea feedArea) {
        try (Connection conn = DBConnection.connect()) {

            String sql = """
                SELECT username, title, content
                FROM discussions
                WHERE status='APPROVED'
                ORDER BY created_at DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            StringBuilder output = new StringBuilder();

            while(rs.next()){
                output.append("User: ")
                      .append(rs.getString("username"))
                      .append("\nTitle: ")
                      .append(rs.getString("title"))
                      .append("\n")
                      .append(rs.getString("content"))
                      .append("\n--------------------\n");
            }

            if(output.length() == 0){
                feedArea.setText("No approved discussions yet.");
            } else {
                feedArea.setText(output.toString());
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void refreshReviewFeed(TextArea displayArea, String bookTitle) {
        try(Connection conn = DBConnection.connect()){

            String sql = """
                SELECT username, rating, review_text
                FROM reviews
                WHERE book_title = ?
                AND status='APPROVED'
                ORDER BY created_at DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bookTitle);

            ResultSet rs = stmt.executeQuery();

            StringBuilder output = new StringBuilder();

            while(rs.next()){
                output.append("User: ")
                      .append(rs.getString("username"))
                      .append("\nRating: ")
                      .append(rs.getInt("rating"))
                      .append("/5")
                      .append("\n")
                      .append(rs.getString("review_text"))
                      .append("\n-------------------\n");
            }

            if(output.length() == 0){
                displayArea.setText("No approved reviews for this book yet.");
            } else {
                displayArea.setText(output.toString());
            }

        } catch(Exception e){
            e.printStackTrace();
        }
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
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
    
    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web("#2c3e50"));
        return lbl;
    }
    
    
}