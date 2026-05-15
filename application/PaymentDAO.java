package application;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class PaymentDAO {
	public static void savePayment(String username, Payment payment) {

        String sql = "INSERT INTO payments (username, payment_method, amount, paid) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, payment.paymentMethod);
            stmt.setDouble(3, payment.amount);
            stmt.setBoolean(4, payment.paid);

            stmt.executeUpdate();
            SystemLogDAO.logAction(
                    username,
                    "Payment completed"
            );

            System.out.println("Payment saved successfully!");

        } catch (Exception e) {
            System.out.println("Failed to save payment.");
            e.printStackTrace();
        }
    }
}