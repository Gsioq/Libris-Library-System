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
import database.DBConnection;
import java.util.*;
import java.sql.*;


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
        tabs.getTabs().add(buildSubscriptionTab());
        if (SubscriptionDAO.hasActiveSubscription(currentUser.getUsername())) {
            tabs.getTabs().add(buildAIAssistantTab());
        }
        //Tabs based on your roles 
        if (AuthService.hasAccess(currentUser, Role.LIBRARIAN)) {
            tabs.getTabs().add(buildUploadContentTab());
            tabs.getTabs().add(buildCatalogManagementTab());
        }
        
        if (AuthService.hasAccess(currentUser, Role.CONTENT_MODERATOR)) {
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

        List<Bookmark> bookmarks =
                BookmarkDAO.getAllBookmarks(
                        currentUser.getUsername()
                );
        
        if (!bookmarks.isEmpty()) {

            box.getChildren().add(new Separator());

            Label bookmarkHeader = new Label("Your Bookmarks:");
            bookmarkHeader.setFont(
                Font.font("Arial", FontWeight.BOLD, 16)
            );

            box.getChildren().add(bookmarkHeader);

            for (Bookmark bookmark : bookmarks) {

                Label bookmarkInfo = new Label(
                    "Continue Reading: " +
                    bookmark.getBookTitle() +
                    " (Page " +
                    bookmark.getPageNumber() + ")"
                );

                Button continueBtn = new Button(
                    "Continue Reading"
                );

                styleButton(
                    continueBtn,
                    "#27ae60",
                    "#fff"
                );

                continueBtn.setOnAction(e -> {
                    try {
                        String title =
                                bookmark.getBookTitle();

                        int page =
                                bookmark.getPageNumber();

                        String filePath =
                                content.getBookFilePath(title);

                        if (filePath == null) {
                            Alert alert = new Alert(
                                Alert.AlertType.ERROR,
                                "Book file not found."
                            );
                            alert.showAndWait();
                            return;
                        }

                        File file = new File(filePath);

                        PDDocument document =
                                Loader.loadPDF(file);

                        PDFTextStripper stripper =
                                new PDFTextStripper();

                        int totalPages =
                                document.getNumberOfPages();

                        final int[] currentPage = {page};

                        TextArea readerArea =
                                new TextArea();

                        readerArea.setWrapText(true);
                        readerArea.setEditable(false);
                        readerArea.setPrefHeight(500);

                        Runnable loadPage = () -> {
                            try {
                                stripper.setStartPage(
                                    currentPage[0]
                                );

                                stripper.setEndPage(
                                    currentPage[0]
                                );

                                String pageText =
                                        stripper.getText(
                                            document
                                        );

                                readerArea.setText(
                                    "Page " +
                                    currentPage[0] +
                                    " of " +
                                    totalPages +
                                    "\n\n" +
                                    pageText
                                );

                            } catch(Exception ex){
                                ex.printStackTrace();
                            }
                        };

                        loadPage.run();

                        double percentage =
                                ((double) currentPage[0] / totalPages) * 100;

                        Label progressLabel = new Label(
                                String.format("Progress: %.1f%%", percentage)
                        );

                        progressLabel.setFont(
                                Font.font("Arial", FontWeight.BOLD, 14)
                        );

                        // buttons
                        Button prevBtn = new Button("Previous Page");
                        Button nextBtn = new Button("Next Page");
                        Button closeBtn = new Button("Close Reader");
                        Button bookmarkBtn = new Button("Bookmark Current Page");

                        styleButton(prevBtn, "#3498db", "#fff");
                        styleButton(nextBtn, "#27ae60", "#fff");
                        styleButton(closeBtn, "#e74c3c", "#fff");
                        styleButton(bookmarkBtn, "#f39c12", "#fff");

                        Label bookmarkLabel = new Label();

                        prevBtn.setOnAction(event -> {
                            if(currentPage[0] > 1){
                                currentPage[0]--;
                                loadPage.run();

                                double updatedPercentage =
                                        ((double) currentPage[0] / totalPages) * 100;

                                progressLabel.setText(
                                        String.format("Progress: %.1f%%", updatedPercentage)
                                );
                            }
                        });

                        nextBtn.setOnAction(event -> {
                            if(currentPage[0] < totalPages){
                                currentPage[0]++;
                                loadPage.run();

                                double updatedPercentage =
                                        ((double) currentPage[0] / totalPages) * 100;

                                progressLabel.setText(
                                        String.format("Progress: %.1f%%", updatedPercentage)
                                );
                            }
                        });

                        bookmarkBtn.setOnAction(event -> {
                            try {
                                Bookmark newBookmark = new Bookmark(
                                        title,
                                        currentPage[0]
                                );

                                BookmarkDAO.saveBookmark(
                                        currentUser.getUsername(),
                                        newBookmark
                                );

                                bookmarkLabel.setText(
                                        "Bookmarked page " + currentPage[0]
                                );

                                bookmarkLabel.setTextFill(Color.GREEN);

                                Scene currentScene = bookmarkBtn.getScene();

                                if (currentScene != null) {
                                    TabPane mainTabs = null;

                                    for (javafx.scene.Node node : ((VBox) currentScene.getRoot()).getChildren()) {
                                        if (node instanceof TabPane) {
                                            mainTabs = (TabPane) node;
                                            break;
                                        }
                                    }

                                    if (mainTabs != null) {
                                        for (Tab existingTab : mainTabs.getTabs()) {
                                            if (existingTab.getText().equals("🏠 Home")) {
                                                existingTab.setContent(
                                                    buildHomeTab().getContent()
                                                );
                                                break;
                                            }
                                        }
                                    }
                                }

                                bookmarkLabel.setTextFill(Color.GREEN);

                            } catch(Exception ex){
                                ex.printStackTrace();

                                bookmarkLabel.setText(
                                        "Failed to save bookmark."
                                );

                                bookmarkLabel.setTextFill(Color.RED);
                            }
                        });

                        HBox navButtons = new HBox(
                                10,
                                prevBtn,
                                nextBtn
                        );

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
                                new Label("📖 Continuing: " + title),
                                readingSection,
                                navButtons
                        );

                        readerLayout.setPadding(
                                new Insets(20)
                        );

                        Tab readerTab = new Tab("📖 " + title);
                        readerTab.setClosable(true);
                        readerTab.setContent(readerLayout);

                        Scene currentScene = continueBtn.getScene();

                        TabPane parentTabs = null;

                        if (currentScene != null) {
                            for (javafx.scene.Node node :
                                    ((VBox) currentScene.getRoot()).getChildren()) {

                                if (node instanceof TabPane) {
                                    parentTabs = (TabPane) node;
                                    break;
                                }
                            }
                        }

                        if (parentTabs != null) {
                            parentTabs.getTabs().add(readerTab);
                            parentTabs.getSelectionModel().select(readerTab);

                            TabPane finalParentTabs = parentTabs;

                            closeBtn.setOnAction(event -> {
                                finalParentTabs.getTabs().remove(readerTab);
                            });
                        }

                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                });
                
                Button deleteBtn = new Button("Delete Bookmark");
                styleButton(deleteBtn, "#e74c3c", "#fff");

                deleteBtn.setOnAction(e -> {
                    try {
                        BookmarkDAO.deleteBookmark(
                            currentUser.getUsername(),
                            bookmark.getBookTitle()
                        );

                        // refresh home tab immediately
                        Scene currentScene = deleteBtn.getScene();

                        if (currentScene != null) {
                            TabPane mainTabs = null;

                            for (javafx.scene.Node node :
                                    ((VBox) currentScene.getRoot()).getChildren()) {

                                if (node instanceof TabPane) {
                                    mainTabs = (TabPane) node;
                                    break;
                                }
                            }

                            if (mainTabs != null) {
                                for (Tab existingTab : mainTabs.getTabs()) {
                                    if (existingTab.getText().equals("🏠 Home")) {
                                        existingTab.setContent(
                                            buildHomeTab().getContent()
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                HBox buttonRow = new HBox(
                	    10,
                	    continueBtn,
                	    deleteBtn
                	);

                	VBox bookmarkCard = new VBox(
                	    8,
                	    bookmarkInfo,
                	    buttonRow
                	);

                bookmarkCard.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-padding: 15;" +
                    "-fx-background-radius: 8;"
                );

                box.getChildren().add(
                    bookmarkCard
                );
            }
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
        Label accountHeader = new Label("Account Settings");
        accountHeader.setFont(
            Font.font("Georgia", FontWeight.BOLD, 16)
        );

        Button editAccountBtn = new Button("Edit Account");
        styleButton(editAccountBtn, "#3498db", "#fff");

        Button deleteAccountBtn = new Button("Delete Account");
        styleButton(deleteAccountBtn, "#e74c3c", "#fff");

        box.getChildren().addAll(
            new Separator(),
            accountHeader,
            new HBox(10, editAccountBtn, deleteAccountBtn)
        );
        
        editAccountBtn.setOnAction(e -> {
            TextInputDialog usernameDialog = new TextInputDialog(
                currentUser.getUsername()
            );

            usernameDialog.setTitle("Edit Account");
            usernameDialog.setHeaderText("Update your username");
            usernameDialog.setContentText("New username:");

            Optional<String> usernameResult =
                    usernameDialog.showAndWait();

            if(usernameResult.isEmpty()) return;

            String newUsername =
                    usernameResult.get().trim();

            if(newUsername.isEmpty()){
                showAlert(
                    "Error",
                    "Username cannot be empty."
                );
                return;
            }

            boolean success = UserDAO.updateUsername(
                    currentUser.getUsername(),
                    newUsername
            );

            if(success){
                showAlert(
                    "Success",
                    "Username updated successfully. Please login again."
                );

                stage.setScene(
                    new LoginScreen(stage).getScene()
                );
            } else {
                showAlert(
                    "Error",
                    "Failed to update username."
                );
            }
        });
        
        deleteAccountBtn.setOnAction(e -> {
            Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION
            );

            confirm.setTitle("Delete Account");
            confirm.setHeaderText(null);
            confirm.setContentText(
                "Are you sure you want to permanently delete your account?"
            );

            Optional<ButtonType> result =
                    confirm.showAndWait();

            if(result.isPresent() &&
               result.get() == ButtonType.OK){

                boolean success = UserDAO.deleteUser(
                        currentUser.getUsername()
                );

                if(success){
                    showAlert(
                        "Deleted",
                        "Your account has been deleted."
                    );

                    stage.setScene(
                        new LoginScreen(stage).getScene()
                    );

                } else {
                    showAlert(
                        "Error",
                        "Failed to delete account."
                    );
                }
            }
        });
        
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
            String output = content.searchContent(keyword);
            resultArea.setText(output);
        });

        // filtration (Connects to ContentService.filterByGenre)
        filterBtn.setOnAction(e -> {
            String genre = genreField.getText().trim();
            if (genre.isEmpty()) {
                resultArea.setText("Please enter a genre.");
                return;
            }
            String output = content.filterByGenre(genre);
            resultArea.setText(output);
        });
        
        readBtn.setOnAction(e -> {
        	String searchedKeyword = keywordField.getText().trim();
            
            boolean isPremium =
                    SubscriptionDAO.hasActiveSubscription(
                            currentUser.getUsername()
                    );

            if (searchedKeyword.isEmpty()) {
                resultArea.setText("Enter a book title first.");
                return;
            }

            String actualTitle = content.getExactBookTitle(searchedKeyword);

            if (actualTitle == null) {
                resultArea.setText("Book not found.");
                return;
            }

            // Check if user already opened this exact book before
            boolean alreadyOpened =
                    ReadingHistoryDAO.hasOpenedBook(
                            currentUser.getUserId(),
                            actualTitle
                    );

            // Free users → only block NEW books after reaching 5 unique books
            if (!isPremium && !alreadyOpened) {

            	int booksRead =
            	        ReadingHistoryDAO.getUniqueBooksRead(
            	                currentUser.getUserId()
            	        );

                if (booksRead >= 5) {
                    resultArea.setText(
                        "Free users can only access 5 different books. Subscribe for unlimited reading + AI Assistant."
                    );
                    return;
                }

                // only log brand new book
                ReadingHistoryDAO.logBookOpen(
                        currentUser.getUserId(),
                        actualTitle
                );
            }

            if (searchedKeyword.isEmpty()) {
                resultArea.setText("Enter a book title first.");
                return;
            }

            try {
            	String filePath = content.getBookFilePath(actualTitle);

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
                        Bookmark newBookmark = new Bookmark(
                        		actualTitle,
                                currentPage[0]
                        );

                        BookmarkDAO.saveBookmark(
                                currentUser.getUsername(),
                                newBookmark
                        );

                        bookmarkLabel.setText(
                                "Bookmarked page " + currentPage[0]
                        );

                        bookmarkLabel.setTextFill(Color.GREEN);

                        // refresh Home tab WITHOUT closing current reader
                        Scene currentScene = bookmarkBtn.getScene();

                        if (currentScene != null) {
                            TabPane mainTabs = null;

                            for (javafx.scene.Node node :
                                    ((VBox) currentScene.getRoot()).getChildren()) {

                                if (node instanceof TabPane) {
                                    mainTabs = (TabPane) node;
                                    break;
                                }
                            }

                            if (mainTabs != null) {
                                for (Tab existingTab : mainTabs.getTabs()) {

                                    if (existingTab.getText().equals("🏠 Home")) {
                                        existingTab.setContent(
                                                buildHomeTab().getContent()
                                        );
                                        break;
                                    }
                                }
                            }
                        }

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
                        new Label("📖 Now Reading: " + actualTitle),
                        readingSection,
                        navButtons
                );

                readerLayout.setPadding(new Insets(20));

                Tab readerTab = new Tab("📖 " + actualTitle);
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

                String actualTitle = content.getExactBookTitle(title);

                if(actualTitle == null){
                    showMessage(
                        messageLabel,
                        "Book not found.",
                        false
                    );
                    return;
                }

                Bookmark bookmark = new Bookmark(
                    actualTitle,
                    page
                );

                BookmarkDAO.saveBookmark(
                    currentUser.getUsername(),
                    bookmark
                );
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

// Thank you surfshark for giving me an expensive subscription 
    private Tab buildSubscriptionTab() {
        Tab tab = new Tab("⭐ Subscription");

        // 1. Subscription Plan Selection
        ComboBox<String> planBox = new ComboBox<>();
        planBox.getItems().addAll("Basic Monthly (Php 40)", "Premium Monthly (Php 60)", "Premium Annual (Php 100)");
        planBox.setValue("Basic Monthly (Php 40)");
        planBox.setMaxWidth(Double.MAX_VALUE);
        planBox.setStyle("-fx-font-size: 13px;");

        TextField priceField = new TextField("40.0"); 
        styleField(priceField);
        priceField.setEditable(false); 
        priceField.setStyle("-fx-background-color: #ecf0f1;"); 
        
        planBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.contains("Basic Monthly")) {
                    priceField.setText("40.0");
                } else if (newValue.contains("Premium Monthly")) {
                    priceField.setText("60.0");
                } else if (newValue.contains("Annual")) {
                    priceField.setText("100.0");
                }
            }
        });

        // 2. Payment Method Selection (Imported from the old payment tab)
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("GCash", "PayMaya", "Credit Card", "Bank Transfer");
        methodBox.setValue("GCash");
        methodBox.setMaxWidth(Double.MAX_VALUE);
        methodBox.setStyle("-fx-font-size: 13px;");

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));

        Button activateBtn = new Button("Pay & Activate");
        styleButton(activateBtn, "#27ae60", "#fff");

        Button cancelBtn = new Button("Cancel Subscription");
        styleButton(cancelBtn, "#e74c3c", "#fff");

        // 3. Combined Action: Try to pay FIRST, then subscribe if it succeeds
        activateBtn.setOnAction(e -> {
            try {
                String plan  = planBox.getValue();
                double price = Double.parseDouble(priceField.getText().trim());
                String method = methodBox.getValue();

                activateBtn.setDisable(true);
                showMessage(messageLabel, "Contacting " + method + " servers...", true);

                // Run the whole process in the background
                Task<Void> paymentAndSubTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        // Step A: Attempt the Payment
                        Payment payment = new Payment(method, price);
                        payment.processPayment(); // If this declines (20% chance), it throws an Error and stops right here!
                        PaymentDAO.savePayment(currentUser.getUsername(), payment);

                        // Step B: Only if payment succeeds, activate the subscription
                        Subscription sub = new Subscription(plan, price);
                        sub.activate();
                        SubscriptionDAO.saveSubscription(currentUser.getUsername(), sub);
                        
                        return null;
                    }
                };

                // If both steps succeed
                paymentAndSubTask.setOnSucceeded(ev -> {
                    showMessage(
                        messageLabel,
                        "Payment successful! Subscribed to: " + plan,
                        true
                    );

                    activateBtn.setDisable(false);

                    stage.setScene(
                        new MainDashboard(stage, currentUser).getScene()
                    );
                });

                // If the simulated bank throws an error
                paymentAndSubTask.setOnFailed(ev -> {
                    String errorMsg = paymentAndSubTask.getException().getMessage();
                    showMessage(messageLabel, "Payment Failed: " + errorMsg, false);
                    activateBtn.setDisable(false);
                });

                new Thread(paymentAndSubTask).start();

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Invalid price detected.", false);
            }
        });

        cancelBtn.setOnAction(e -> {
            try {
                String plan  = planBox.getValue();
                double price = Double.parseDouble(priceField.getText().trim());

                Subscription sub = new Subscription(plan, price);
                sub.cancel();
                SubscriptionDAO.saveSubscription(currentUser.getUsername(), sub);
                showMessage(messageLabel, "Subscription cancelled: " + plan, false);
                stage.setScene(
                	    new MainDashboard(stage, currentUser).getScene()
                	);

            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Invalid price detected.", false);
            }
        });

        HBox btnRow = new HBox(10, activateBtn, cancelBtn);

        // Build the layout combining both modules
        VBox box = new VBox(12,
                sectionLabel("Subscribe to Premium"),
                new Label("1. Select Plan:"), planBox, priceField,
                new Label("2. Payment Method:"), methodBox,
                btnRow, messageLabel
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
    
    private Tab buildCatalogManagementTab() {
        Tab tab = new Tab("📚 Catalog Management");

        TextArea catalogArea = new TextArea();
        catalogArea.setEditable(false);
        catalogArea.setPrefHeight(300);

        TextField titleField = new TextField();
        titleField.setPromptText("Enter exact book title to open");
        styleField(titleField);

        Button loadBtn = new Button("Load Catalog");
        styleButton(loadBtn, "#3498db", "#fff");

        Button openBtn = new Button("Open Book");
        styleButton(openBtn, "#27ae60", "#fff");
        
        Button editBtn = new Button("Replace PDF");
        styleButton(editBtn, "#f39c12", "#fff");
        
        Button deleteBtn = new Button("Delete Book");
        styleButton(deleteBtn, "#e74c3c", "#fff");

        Label messageLabel = new Label();

        // Load all uploaded books
        loadBtn.setOnAction(e -> {
            try(Connection conn = DBConnection.connect()) {

                String sql = """
                    SELECT title, author, genre, status
                    FROM books
                    ORDER BY book_id DESC
                """;

                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                StringBuilder output = new StringBuilder();

                while(rs.next()) {
                    output.append("Title: ")
                          .append(rs.getString("title"))
                          .append("\nAuthor: ")
                          .append(rs.getString("author"))
                          .append("\nGenre: ")
                          .append(rs.getString("genre"))
                          .append("\nStatus: ")
                          .append(rs.getString("status"))
                          .append("\n----------------------\n");
                }

                catalogArea.setText(output.toString());

            } catch(Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Failed to load catalog.");
            }
        });

        openBtn.setOnAction(e -> {
            String title = titleField.getText().trim();

            if(title.isEmpty()) {
                messageLabel.setText("Enter a book title.");
                return;
            }

            try {
                String actualTitle = content.getExactBookTitle(title);
                String filePath = content.getBookFilePath(actualTitle);

                if(filePath == null) {
                    messageLabel.setText("Book not found.");
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

                Runnable loadPage = () -> {
                    try {
                        stripper.setStartPage(currentPage[0]);
                        stripper.setEndPage(currentPage[0]);

                        readerArea.setText(
                            "Page " + currentPage[0] +
                            " of " + totalPages +
                            "\n\n" +
                            stripper.getText(document)
                        );

                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                };

                loadPage.run();

                Button prevBtn = new Button("Previous Page");
                Button nextBtn = new Button("Next Page");
                Button closeBtn = new Button("Close Reader");

                styleButton(prevBtn, "#3498db", "#fff");
                styleButton(nextBtn, "#27ae60", "#fff");
                styleButton(closeBtn, "#e74c3c", "#fff");

                prevBtn.setOnAction(ev -> {
                    if(currentPage[0] > 1){
                        currentPage[0]--;
                        loadPage.run();
                    }
                });

                nextBtn.setOnAction(ev -> {
                    if(currentPage[0] < totalPages){
                        currentPage[0]++;
                        loadPage.run();
                    }
                });

                Tab readerTab = new Tab("📖 " + actualTitle);
                readerTab.setClosable(true);

                VBox readerLayout = new VBox(
                    15,
                    new Label("Managing: " + actualTitle),
                    readerArea,
                    new HBox(10, prevBtn, nextBtn, closeBtn)
                );

                readerLayout.setPadding(new Insets(20));
                readerTab.setContent(readerLayout);

                TabPane parentTabs = (TabPane) tab.getTabPane();
                parentTabs.getTabs().add(readerTab);
                parentTabs.getSelectionModel().select(readerTab);

                closeBtn.setOnAction(ev -> {
                    parentTabs.getTabs().remove(readerTab);
                    try {
                        document.close();
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                });

            } catch(Exception ex){
                ex.printStackTrace();
                messageLabel.setText("Failed to open book.");
            }
        });
        
        editBtn.setOnAction(e -> {
            String title = titleField.getText().trim();

            if(title.isEmpty()) {
                messageLabel.setText("Enter a book title.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File newFile = fileChooser.showOpenDialog(stage);

            if(newFile == null) return;

            try(Connection conn = DBConnection.connect()) {

                String sql = """
                    UPDATE books
                    SET file_path = ?
                    WHERE LOWER(title) = LOWER(?)
                """;

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newFile.getAbsolutePath());
                stmt.setString(2, title);

                int rows = stmt.executeUpdate();

                if(rows > 0){
                    messageLabel.setText("PDF replaced successfully.");
                } else {
                    messageLabel.setText("Book not found.");
                }

            } catch(Exception ex){
                ex.printStackTrace();
            }
        });
        
        deleteBtn.setOnAction(e -> {
            String title = titleField.getText().trim();

            if(title.isEmpty()){
                messageLabel.setText("Enter a book title.");
                return;
            }

            try(Connection conn = DBConnection.connect()) {

                String sql = """
                    DELETE FROM books
                    WHERE LOWER(title) = LOWER(?)
                """;

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, title);

                int rows = stmt.executeUpdate();

                if(rows > 0){
                    messageLabel.setText("Book deleted successfully.");
                    catalogArea.clear();
                } else {
                    messageLabel.setText("Book not found.");
                }

            } catch(Exception ex){
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(
        	    12,
        	    loadBtn,
        	    catalogArea,
        	    titleField,
        	    new HBox(10, openBtn, editBtn, deleteBtn),
        	    messageLabel
        	);

        layout.setPadding(new Insets(20));

        tab.setContent(layout);

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

            String output = auth.viewLoginActivity(username);

            logArea.setText(
                output.isEmpty()
                ? "No login activity found for: " + username
                : output
            );
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