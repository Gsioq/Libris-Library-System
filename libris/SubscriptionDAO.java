package libris;
import java.sql.Connection;
import java.sql.PreparedStatement;
import database.DBConnection;


public class SubscriptionDAO {
	public static void saveSubscription(String username, Subscription sub) {

        String sql = "INSERT INTO subscriptions (username, plan_name, price, active) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, sub.planName);
            stmt.setDouble(3, sub.price);
            stmt.setBoolean(4, sub.active);

            stmt.executeUpdate();
            SystemLogDAO.logAction(
                    username,
                    "Subscription to: " + sub.planName
            );

            System.out.println("Subscription saved successfully!");

        } catch (Exception e) {
            System.out.println("Failed to save subscription.");
            e.printStackTrace();
        }
    }
}
