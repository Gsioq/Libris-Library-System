package application;
import java.sql.*;
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
	
	public static boolean hasActiveSubscription(String username) {
	    try(Connection conn = DBConnection.connect()) {

	        String sql = """
	            SELECT active
	            FROM subscriptions
	            WHERE username = ?
	            ORDER BY id DESC
	            LIMIT 1
	        """;

	        PreparedStatement stmt =
	                conn.prepareStatement(sql);

	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();

	        if(rs.next()){
	            return rs.getBoolean("active");
	        }

	    } catch(Exception e){
	        e.printStackTrace();
	    }

	    return false;
	}
}