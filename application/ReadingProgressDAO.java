package application;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class ReadingProgressDAO {
	public static void saveProgress(String username, ReadingProgress progress) {

        String sql = "INSERT INTO reading_progress "
                   + "(username, book_title, current_page, total_pages, progress_percentage) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, progress.bookTitle);
            stmt.setInt(3, progress.currentPage);
            stmt.setInt(4, progress.totalPages);
            stmt.setDouble(5, progress.getPercentage());

            stmt.executeUpdate();

            System.out.println("Reading progress saved successfully!");

        } catch (Exception e) {
            System.out.println("Failed to save reading progress.");
            e.printStackTrace();
        }
    }

}