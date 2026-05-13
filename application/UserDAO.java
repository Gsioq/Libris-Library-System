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
}