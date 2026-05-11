package libris;

public class MainTest {

    public static void main(String[] args) {

        AuthService auth = new AuthService();
        ContentService content = new ContentService();

        System.out.println("========== REGISTER ==========");

        System.out.println(auth.register(
                "readerFinal",
                "reader123",
                "readerfinal@gmail.com",
                "09111111111",
                Role.READER
        ));

        System.out.println(auth.register(
                "adminFinal",
                "admin123",
                "adminfinal@gmail.com",
                "09222222222",
                Role.ADMIN
        ));

        System.out.println(auth.register(
                "librarianFinal",
                "lib123",
                "librarianfinal@gmail.com",
                "09333333333",
                Role.LIBRARIAN
        ));

        System.out.println(auth.register(
                "moderatorFinal",
                "mod123",
                "moderatorfinal@gmail.com",
                "09444444444",
                Role.CONTENT_MODERATOR
        ));


        System.out.println("\n========== VERIFY ==========");

        System.out.println(auth.verify("readerFinal"));
        System.out.println(auth.verify("adminFinal"));
        System.out.println(auth.verify("librarianFinal"));
        System.out.println(auth.verify("moderatorFinal"));


        System.out.println("\n========== LOGIN ==========");

        System.out.println(auth.login(
                "readerFinal",
                "reader123"
        ));


        System.out.println("\n========== FAILED LOGIN TEST ==========");

        System.out.println(auth.login(
                "readerFinal",
                "wrongpass"
        ));

        System.out.println(auth.login(
                "readerFinal",
                "wrongpass"
        ));


        System.out.println("\n========== LOGIN ACTIVITY ==========");
        auth.viewLoginActivity("readerFinal");


        User librarian =
                auth.loginUser(
                        "librarianFinal",
                        "lib123"
                );

        User moderator =
                auth.loginUser(
                        "moderatorFinal",
                        "mod123"
                );


        System.out.println("\n========== VALID CONTENT UPLOAD ==========");

        if (librarian != null) {
            System.out.println(
                    content.uploadContent(
                            librarian,
                            "Data Structures in Java",
                            "James Smith",
                            "Academic",
                            "A complete academic guide to Java data structures."
                    )
            );
        }


        System.out.println("\n========== AUTOMATED MODERATION TEST ==========");

        if (librarian != null) {
            System.out.println(
                    content.uploadContent(
                            librarian,
                            "Suspicious Book",
                            "Unknown Author",
                            "Fiction",
                            "This promotes violence and illegal activities."
                    )
            );
        }


        System.out.println("\n========== SEARCH TEST ==========");
        content.searchContent("Java");


        System.out.println("\n========== FILTER BY GENRE ==========");
        content.filterByGenre("Academic");


        System.out.println("\n========== MANUAL MODERATION ==========");

        if (moderator != null) {
            System.out.println(
                    content.moderateContent(
                            moderator,
                            1,
                            "APPROVED"
                    )
            );
        }


        System.out.println("\n========== PAYMENT ==========");

        Payment payment =
                new Payment(
                        "GCash",
                        299.00
                );

        payment.processPayment();

        PaymentDAO.savePayment(
                "readerFinal",
                payment
        );


        System.out.println("\n========== SUBSCRIPTION ==========");

        Subscription subscription =
                new Subscription(
                        "Premium Annual",
                        999.00
                );

        subscription.activate();

        SubscriptionDAO.saveSubscription(
                "readerFinal",
                subscription
        );


        System.out.println("\n========== READING PROGRESS ==========");

        ReadingProgress progress =
                new ReadingProgress(
                        "Data Structures in Java",
                        500
                );

        progress.updateProgress(250);

        ReadingProgressDAO.saveProgress(
                "readerFinal",
                progress
        );


        System.out.println("\n========== BOOKMARK ==========");

        Bookmark bookmark =
                new Bookmark(
                        "Data Structures in Java",
                        250
                );

        BookmarkDAO.saveBookmark(
                "readerFinal",
                bookmark
        );


        System.out.println("\n========== REPORT ==========");
        ReportDAO.viewUserActivity("readerFinal");


        System.out.println("\n========== SYSTEM LOG CHECK ==========");
        System.out.println(
                "Check system_logs table in pgAdmin."
        );


        System.out.println("\n========== FINAL TEST COMPLETE ==========");
    }
}