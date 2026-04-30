package userAuth;
import java.util.*;

public class AuthService {
    private static final long LOCK_DURATION = 15 * 60 * 1000;

    private HashMap<String, User> users = new HashMap<>();
    private HashMap<String, List<LoginActivity>> loginLogs = new HashMap<>();

    // REGISTRATION
    public String register(String username, String password, Role role) {
        if (users.containsKey(username)) {
            return "Username already exists.";
        }

        User newUser = new User(username, password, role);
        users.put(username, newUser);

        return "Account created. Please verify account.";
    }

    // VERIFICATION
    public String verify(String username) {
        User user = users.get(username);
        if (user == null) return "User not found.";

        user.verifyAccount();
        return "Account verified successfully.";
    }

    // LOGIN
    public String login(String username, String password) {
        User user = users.get(username);

        if (user == null) {
            logActivity(username, "USER NOT FOUND");
            return "User not found.";
        }

        long currentTime = System.currentTimeMillis();

        if (user.getLockTime() > currentTime) {
            logActivity(username, "ACCOUNT LOCKED");
            long remaining = (user.getLockTime() - currentTime) / 1000;
            return "Account locked. Try again in " + remaining + " seconds.";
        }

        if (!user.isVerified()) {
            logActivity(username, "NOT VERIFIED");
            return "Account not verified.";
        }

        if (!user.getPassword().equals(password)) {
            user.incrementAttempts();
            logActivity(username, "FAILED LOGIN");

            if (user.getAttempts() >= 3) {
                user.setLockTime(currentTime + LOCK_DURATION);
                user.resetAttempts();
                logActivity(username, "ACCOUNT LOCKED (MAX ATTEMPTS)");
                return "Too many failed attempts. Account locked for 15 minutes.";
            }

            return "Incorrect password. Attempt " + user.getAttempts() + "/3";
        }

        user.resetAttempts();
        logActivity(username, "SUCCESS");
        return "Login successful! Role: " + user.getRole();
    }

    // GET LOGGED-IN USER
    public User loginUser(String username, String password) {
        String result = login(username, password);
        if (result.startsWith("Login successful")) {
            return users.get(username);
        }
        return null;
    }

    // LOGIN ACTIVITY
    private void logActivity(String username, String status) {
        loginLogs.putIfAbsent(username, new ArrayList<>());
        loginLogs.get(username).add(
                new LoginActivity(username, status, System.currentTimeMillis())
        );
    }

    public void viewLoginActivity(String username) {
        if (!loginLogs.containsKey(username)) {
            System.out.println("No login activity found.");
            return;
        }

        for (LoginActivity log : loginLogs.get(username)) {
            System.out.println(log);
        }
    }

    
    // ROLE CHECK
    public boolean hasAccess(User user, Role... allowedRoles) {
        for (Role role : allowedRoles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    // PROTECTED FEATURES (what am I doing?)
    public String deleteUser(User currentUser, String usernameToDelete) {
        if (!hasAccess(currentUser, Role.ADMIN)) {
            return "Access denied. Admins only.";
        }

        if (!users.containsKey(usernameToDelete)) {
            return "User not found.";
        }

        users.remove(usernameToDelete);
        return "User deleted successfully.";
    }

    public String addBook(User currentUser) {
        if (!hasAccess(currentUser, Role.LIBRARIAN, Role.ADMIN)) {
            return "Access denied. Librarians or Admins only.";
        }

        return "Book added successfully.";
    }

    public String moderateContent(User currentUser) {
        if (!hasAccess(currentUser, Role.CONTENT_MODERATOR, Role.ADMIN)) {
            return "Access denied. Moderators or Admins only.";
        }

        return "Content moderated.";
    }
}
