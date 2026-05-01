package userAuth;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.DBConnection;

public class AuthService {
    private static final long LOCK_DURATION = 15 * 60 * 1000;

    // REGISTRATION
    public String register(String username, String password, String email, String phoneNumber, Role role) {
    	try {
    		Connection conn = DBConnection.connect();
    		
    		//Check the username if it is used
    		String checkQuery = "SELECT * FROM users WHERE username = ?";
    		PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
    		checkStmt.setString(1, username);
    		ResultSet rs = checkStmt.executeQuery();
    		
    		if(rs.next()) {
    			return "Username already exists.";
    		}
    		
    		String insertQuery = """
    	            INSERT INTO users(username, password, email, phone_number, role)
    	            VALUES (?, ?, ?, ?, ?)
    	        """;
    		
    		PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, email);
            insertStmt.setString(4, phoneNumber);
            insertStmt.setString(5, role.toString());

            insertStmt.executeUpdate();

            return "Account created successfully.";
            
    	 } catch (SQLException e) {
    	        e.printStackTrace();
    	        return "Registration failed.";
    	    }
    	}
    

    // VERIFICATION
    public String verify(String username) {
        try {
            Connection conn = DBConnection.connect();

            String query =
                    "UPDATE users SET is_verified = true WHERE username = ?";

            PreparedStatement stmt =
                    conn.prepareStatement(query);

            stmt.setString(1, username);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                return "Account verified successfully.";
            }

            return "User not found.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Verification failed.";
        }
    }

    // LOGIN
    public String login(String username, String password) {
        try {
            Connection conn = DBConnection.connect();

            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                logActivity(username, "USER NOT FOUND");
                return "User not found.";
            }

            String storedPassword = rs.getString("password");
            boolean isVerified = rs.getBoolean("is_verified");
            int attempts = rs.getInt("attempts");
            long lockTime = rs.getLong("lock_time");

            long currentTime = System.currentTimeMillis();

            // Check lock
            if (lockTime > currentTime) {
                logActivity(username, "ACCOUNT LOCKED");

                long remaining =
                        (lockTime - currentTime) / 1000;

                return "Account locked. Try again in "
                        + remaining + " seconds.";
            }

            // Check verification
            if (!isVerified) {
                logActivity(username, "NOT VERIFIED");
                return "Account not verified.";
            }

            // Wrong password
            if (!storedPassword.equals(password)) {
                attempts++;

                String updateAttempts =
                        "UPDATE users SET attempts = ? WHERE username = ?";

                PreparedStatement attemptStmt =
                        conn.prepareStatement(updateAttempts);

                attemptStmt.setInt(1, attempts);
                attemptStmt.setString(2, username);
                attemptStmt.executeUpdate();

                logActivity(username, "FAILED LOGIN");

                if (attempts >= 3) {
                    long newLockTime =
                            currentTime + LOCK_DURATION;

                    String lockQuery =
                            "UPDATE users SET lock_time = ?, attempts = 0 WHERE username = ?";

                    PreparedStatement lockStmt =
                            conn.prepareStatement(lockQuery);

                    lockStmt.setLong(1, newLockTime);
                    lockStmt.setString(2, username);
                    lockStmt.executeUpdate();

                    logActivity(username,
                            "ACCOUNT LOCKED (MAX ATTEMPTS)");

                    return "Too many failed attempts. Account locked for 15 minutes.";
                }

                return "Incorrect password. Attempt " + attempts + "/3";
            }

            // Successful login
            String resetAttempts =
                    "UPDATE users SET attempts = 0 WHERE username = ?";

            PreparedStatement resetStmt =
                    conn.prepareStatement(resetAttempts);

            resetStmt.setString(1, username);
            resetStmt.executeUpdate();

            logActivity(username, "SUCCESS");

            return "Login successful! Role: "
                    + rs.getString("role");

        } catch (Exception e) {
            e.printStackTrace();
            return "Login failed.";
        }
    }

    // GET LOGGED-IN USER
    public User loginUser(String username, String password) {
        String result = login(username, password);

        if (!result.startsWith("Login successful")) {
            return null;
        }

        try {
            Connection conn = DBConnection.connect();

            String query =
                    "SELECT * FROM users WHERE username = ?";

            PreparedStatement stmt =
                    conn.prepareStatement(query);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // LOGIN ACTIVITY
    private void logActivity(String username, String status) {
        try {
            Connection conn = DBConnection.connect();

            String userQuery =
                    "SELECT user_id FROM users WHERE username = ?";

            PreparedStatement userStmt =
                    conn.prepareStatement(userQuery);

            userStmt.setString(1, username);

            ResultSet rs = userStmt.executeQuery();

            if (!rs.next()) {
                return;
            }

            int userId = rs.getInt("user_id");

            String logQuery =
                    "INSERT INTO login_activity(user_id, status) VALUES (?, ?)";

            PreparedStatement logStmt =
                    conn.prepareStatement(logQuery);

            logStmt.setInt(1, userId);
            logStmt.setString(2, status);

            logStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewLoginActivity(String username) {
        try {
            Connection conn = DBConnection.connect();

            String query = """
                SELECT la.status, la.login_time
                FROM login_activity la
                JOIN users u ON la.user_id = u.user_id
                WHERE u.username = ?
            """;

            PreparedStatement stmt =
                    conn.prepareStatement(query);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println(
                        "Status: " + rs.getString("status") +
                        " | Time: " + rs.getTimestamp("login_time")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
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

        try {
            Connection conn = DBConnection.connect();

            String query =
                    "DELETE FROM users WHERE username = ?";

            PreparedStatement stmt =
                    conn.prepareStatement(query);

            stmt.setString(1, usernameToDelete);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                return "User deleted successfully.";
            }

            return "User not found.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Delete failed.";
        }
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
