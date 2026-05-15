package application;

import java.sql.*;
import database.DBConnection;
import org.mindrot.jbcrypt.BCrypt;


public class AuthService {

    private static final long LOCK_DURATION = 15 * 60 * 1000;

    // REGISTER 
    public String register(String username, String password,
                           String email, Role role) {
        try (Connection conn = DBConnection.connect()) {

            String check = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(check);
            checkStmt.setString(1, username);

            if (checkStmt.executeQuery().next()) {
                return "Username already exists.";
            }

            String insert = """
            	    INSERT INTO users(username, password, email, phone_number, role)
            	    VALUES (?, ?, ?, ?, ?)
            	""";

            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, username);
            String hashedPassword = BCrypt.hashpw(
                    password,
                    BCrypt.gensalt()
            );

            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.setNull(4, java.sql.Types.VARCHAR);
            stmt.setString(5, role.toString());

            stmt.executeUpdate();
            SystemLogDAO.logAction(
                    username,
                    "User registered"
            );
            return "Account created successfully.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Registration failed.";
        }
    }

    // VERIFY 
    public String verify(String username) {
        try (Connection conn = DBConnection.connect()) {

            String query = "UPDATE users SET is_verified = true WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            return stmt.executeUpdate() > 0 ?
                    "Account verified successfully." :
                    "User not found.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Verification failed.";
        }
    }

    // LOGIN 
    public String login(String username, String password) {
        try (Connection conn = DBConnection.connect()) {

            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                logActivity(username, "USER NOT FOUND");
                return "User not found.";
            }

            long currentTime = System.currentTimeMillis();
            long lockTime = rs.getLong("lock_time");

            if (lockTime > currentTime) {
                logActivity(username, "ACCOUNT LOCKED");
                long remaining = (lockTime - currentTime) / 1000;
                return "Account locked. Try again in " + remaining + " seconds.";
            }

            if (!rs.getBoolean("is_verified")) {
                logActivity(username, "NOT VERIFIED");
                return "Account not verified.";
            }

            String storedPassword = rs.getString("password");

            if (!BCrypt.checkpw(password, storedPassword)) {

                int attempts = rs.getInt("attempts") + 1;

                PreparedStatement attemptStmt = conn.prepareStatement(
                        "UPDATE users SET attempts=? WHERE username=?"
                );
                attemptStmt.setInt(1, attempts);
                attemptStmt.setString(2, username);
                attemptStmt.executeUpdate();

                logActivity(username, "FAILED LOGIN");

                if (attempts >= 3) {
                    long newLock = currentTime + LOCK_DURATION;

                    PreparedStatement lockStmt = conn.prepareStatement(
                            "UPDATE users SET lock_time=?, attempts=0 WHERE username=?"
                    );
                    lockStmt.setLong(1, newLock);
                    lockStmt.setString(2, username);
                    lockStmt.executeUpdate();

                    logActivity(username, "ACCOUNT LOCKED");
                    return "Too many attempts. Locked for 15 minutes.";
                }

                return "Incorrect password. Attempt " + attempts + "/3";
            }

            PreparedStatement resetStmt = conn.prepareStatement(
                    "UPDATE users SET attempts=0 WHERE username=?"
            );
            resetStmt.setString(1, username);
            resetStmt.executeUpdate();

            logActivity(username, "SUCCESS");
            
            SystemLogDAO.logAction(
                    username,
                    "User logged in successfully"
            );
            
            if (rs.getBoolean("two_factor_enabled")) {
                return "2FA_REQUIRED";
            }
            
            return "Login successful! Role: " + rs.getString("role");

        } catch (Exception e) {
            e.printStackTrace();
            return "Login failed.";
        }
    }

    // LOGIN USER OBJECT 
    public User loginUser(String username, String password) {
        try (Connection conn = DBConnection.connect()) {

            String query = "SELECT * FROM users WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next() && BCrypt.checkpw(password,rs.getString("password"))) {
            	User user = new User();

            	user.setUserId(rs.getInt("user_id"));
            	user.setUsername(rs.getString("username"));
            	user.setEmail(rs.getString("email"));
            	user.setRole(Role.valueOf(rs.getString("role")));
            	user.setVerified(rs.getBoolean("is_verified"));
            	user.setTwoFactorEnabled(
            		    rs.getBoolean("two_factor_enabled")
            		);

            	return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // LOGIN ACTIVITY 
    private void logActivity(String username, String status) {
        try (Connection conn = DBConnection.connect()) {

            PreparedStatement userStmt =
                    conn.prepareStatement("SELECT user_id FROM users WHERE username=?");
            userStmt.setString(1, username);

            ResultSet rs = userStmt.executeQuery();
            if (!rs.next()) return;

            int userId = rs.getInt("user_id");

            PreparedStatement logStmt = conn.prepareStatement(
                    "INSERT INTO login_activity(user_id, status) VALUES (?, ?)"
            );
            logStmt.setInt(1, userId);
            logStmt.setString(2, status);
            logStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String viewLoginActivity(String username) {
        StringBuilder output = new StringBuilder();

        try (Connection conn = DBConnection.connect()) {

            String query = """
                SELECT la.status, la.login_time
                FROM login_activity la
                JOIN users u ON la.user_id = u.user_id
                WHERE u.username = ?
                ORDER BY la.login_time DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;

                output.append("Status: ")
                      .append(rs.getString("status"))
                      .append(" | Time: ")
                      .append(rs.getTimestamp("login_time"))
                      .append("\n");
            }

            if (!found) {
                return "No login activity found.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to load login activity.";
        }

        return output.toString();
    }

    // RBAC
    public static boolean hasAccess(User user, Role... roles) {
        for (Role r : roles) {
            if (user.getRole() == r) return true;
        }
        return false;
    }
}