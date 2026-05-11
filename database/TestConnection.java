package database;
import java.sql.Connection;

public class TestConnection {

	public static void main(String[] args) {
		Connection conn = DBConnection.connect();

        if (conn != null) {
            System.out.println("Database is working!");
        }
	}
}
