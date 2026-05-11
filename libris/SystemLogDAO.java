package libris;
import java.sql.*;
import database.DBConnection;

public class SystemLogDAO {
	 public static void logAction(String username, String action) {

	        try(Connection conn = DBConnection.connect()){

	            String query = """
	                INSERT INTO system_logs(username, action)
	                VALUES (?, ?)
	            """;

	            PreparedStatement stmt =
	                    conn.prepareStatement(query);

	            stmt.setString(1, username);
	            stmt.setString(2, action);

	            stmt.executeUpdate();

	            System.out.println("System log saved.");

	        } catch(Exception e){
	            e.printStackTrace();
	        }
        }
}
