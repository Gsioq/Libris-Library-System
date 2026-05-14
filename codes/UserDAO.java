package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import database.DBConnection;

public class UserDAO {

    public static void enableTwoFactor(String username) {
        try (Connection conn = DBConnection.connect()) {

            String sql =
                "UPDATE users SET two_factor_enabled = true WHERE username = ?";

            PreparedStatement stmt =
                conn.prepareStatement(sql);

            stmt.setString(1, username);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean banUser(String username) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                UPDATE users
                SET account_status = 'BANNED'
                WHERE username = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
    
    public static boolean unbanUser(String username) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                UPDATE users
                SET account_status = 'ACTIVE'
                WHERE username = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
}