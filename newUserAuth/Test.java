package userAuth;

public class Test {

    public static void main(String[] args) {

        AuthService auth = new AuthService();

        System.out.println("========== REGISTER ==========");
        System.out.println(auth.register(
                "alice2",
                "1234",
                "alice2@gmail.com",
                "09123456789",
                Role.READER
        ));

        System.out.println(auth.register(
                "bob2",
                "adminpass",
                "bob2@gmail.com",
                "09987654321",
                Role.ADMIN
        ));

        System.out.println(auth.register(
                "cathy2",
                "libpass",
                "cathy2@gmail.com",
                "09111111111",
                Role.LIBRARIAN
        ));

        System.out.println(auth.register(
                "mod1",
                "modpass",
                "mod@gmail.com",
                "09222222222",
                Role.CONTENT_MODERATOR
        ));


        System.out.println("\nLOGIN BEFORE VERIFICATION");
        System.out.println(auth.login("alice2", "1234"));
        // Expected: Account not verified


        System.out.println("\nVERIFY USERS");
        System.out.println(auth.verify("alice2"));
        System.out.println(auth.verify("bob2"));
        System.out.println(auth.verify("cathy2"));
        System.out.println(auth.verify("mod1"));


        System.out.println("\nSUCCESSFUL LOGIN");
        System.out.println(auth.login("alice2", "1234"));


        System.out.println("\nFAILED LOGIN ATTEMPTS");
        System.out.println(auth.login("alice2", "wrongpass"));
        System.out.println(auth.login("alice2", "wrongpass"));
        System.out.println(auth.login("alice2", "wrongpass"));
        // Expected: Account locked


        System.out.println("\nLOGIN WHILE LOCKED");
        System.out.println(auth.login("alice2", "1234"));


        System.out.println("\nVIEW LOGIN ACTIVITY");
        auth.viewLoginActivity("alice2");


        System.out.println("\nGET LOGGED-IN USER");
        User admin = auth.loginUser("bob2", "adminpass");
        User librarian = auth.loginUser("cathy2", "libpass");
        User moderator = auth.loginUser("mod1", "modpass");


        System.out.println("\nROLE CHECK");
        System.out.println("Admin access check: " +
                auth.hasAccess(admin, Role.ADMIN));

        System.out.println("Librarian admin check: " +
                auth.hasAccess(librarian, Role.ADMIN));

        System.out.println("Moderator check: " +
                auth.hasAccess(moderator, Role.CONTENT_MODERATOR));


        System.out.println("\nPROTECTED FEATURES");

        // Delete user
        System.out.println("Admin deletes alice2:");
        System.out.println(auth.deleteUser(admin, "alice2"));

        System.out.println("Librarian tries deleting bob2:");
        System.out.println(auth.deleteUser(librarian, "bob2"));


        // Add book
        System.out.println("Librarian adds book:");
        System.out.println(auth.addBook(librarian));

        System.out.println("Admin adds book:");
        System.out.println(auth.addBook(admin));

        System.out.println("Moderator adds book:");
        System.out.println(auth.addBook(moderator));


        // Moderate content
        System.out.println("Moderator moderates:");
        System.out.println(auth.moderateContent(moderator));

        System.out.println("Admin moderates:");
        System.out.println(auth.moderateContent(admin));

        System.out.println("Librarian moderates:");
        System.out.println(auth.moderateContent(librarian));
    }
}