package application;
import java.sql.Connection;
import java.sql.PreparedStatement;
import database.DBConnection;
import java.sql.ResultSet;


public class BookmarkDAO {
	public static void saveBookmark(String username, Bookmark bookmark) {

        String sql = "INSERT INTO bookmarks (username, book_title, page_number) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, bookmark.bookTitle);
            stmt.setInt(3, bookmark.pageNumber);

            stmt.executeUpdate();

            System.out.println("Bookmark saved successfully!");

        } catch (Exception e) {
            System.out.println("Failed to save bookmark.");
            e.printStackTrace();
        }
    }
	
	public static Bookmark getLatestBookmark(String username) {
	    try (Connection conn = DBConnection.connect()) {

	        PreparedStatement stmt = conn.prepareStatement("""
	            SELECT book_title, page_number
	            FROM bookmarks
	            WHERE username = ?
	            ORDER BY id DESC
	            LIMIT 1
	        """);

	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            return new Bookmark(
	                rs.getString("book_title"),
	                rs.getInt("page_number")
	            );
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return null;
	}
}