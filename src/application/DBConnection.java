package application;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	 private static final String URL =
	            "jdbc:postgresql://localhost:5432/library_db";

	    private static final String USER = "postgres";
	    private static final String PASSWORD = "Roxie0505";

	    public static Connection getConnection() {
	        try {
	            Connection conn =
	                    DriverManager.getConnection(URL, USER, PASSWORD);

	            System.out.println("Connected to PostgreSQL successfully!");
	            return conn;

	        } catch (SQLException e) {
	            System.out.println("Connection failed.");
	            e.printStackTrace();
	            return null;
	        }
	    }

	    // keeps old code working too
	    public static Connection connect() {
	        return getConnection();
	    }
}