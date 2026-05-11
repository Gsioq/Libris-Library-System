package libris;
import java.sql.Connection;
import java.sql.PreparedStatement;
import database.DBConnection;


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
}
