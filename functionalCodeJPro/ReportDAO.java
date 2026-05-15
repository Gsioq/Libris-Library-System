package application;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.DBConnection;


public class ReportDAO {
	 public static void viewUserActivity(String username) {

	        String sql = """
	            SELECT 
	                p.username,
	                p.payment_method,
	                p.amount,
	                p.paid,
	                s.plan_name,
	                s.active AS subscription_active,
	                r.book_title,
	                r.current_page,
	                r.total_pages,
	                r.progress_percentage
	            FROM payments p
	            LEFT JOIN subscriptions s ON p.username = s.username
	            LEFT JOIN reading_progress r ON p.username = r.username
	            WHERE p.username = ?
	            ORDER BY p.id DESC
	            LIMIT 1
	        """;

	        try (Connection conn = DBConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setString(1, username);

	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                System.out.println("User: " + rs.getString("username"));
	                System.out.println("Payment Method: " + rs.getString("payment_method"));
	                System.out.println("Amount: PHP " + rs.getDouble("amount"));
	                System.out.println("Paid: " + rs.getBoolean("paid"));
	                System.out.println("Subscription Plan: " + rs.getString("plan_name"));
	                System.out.println("Subscription Active: " + rs.getBoolean("subscription_active"));
	                System.out.println("Book: " + rs.getString("book_title"));
	                System.out.println("Current Page: " + rs.getInt("current_page"));
	                System.out.println("Total Pages: " + rs.getInt("total_pages"));
	                System.out.println("Progress: " + rs.getDouble("progress_percentage") + "%");
	            } else {
	                System.out.println("No activity found for user: " + username);
	            }

	        } catch (Exception e) {
	            System.out.println("Failed to view user activity.");
	            e.printStackTrace();
	        }
	    }
}