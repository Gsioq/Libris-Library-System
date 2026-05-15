package application;
import java.sql.*;

public class ContentService {
	private boolean isContentAppropriate(String desc) {

        String[] bannedWords = {
                "hate",
                "violence",
                "illegal",
                "explicit"
        };

        desc = desc.toLowerCase();

        for (String word : bannedWords) {
            if (desc.contains(word)) {
                return false;
            }
        }

        return true;
    }

    // CONTENT UPLOAD (I am not confident on this. I am new to this)
    public String uploadContent(User user, String title,
            String author, String genre,
            String desc, String filePath) {

        if (!AuthService.hasAccess(user, Role.ADMIN, Role.LIBRARIAN)) {
            return "Access denied.";
        }
        if (!isContentAppropriate(desc)) {
            return "Upload rejected: inappropriate content detected.";
        }

        try (Connection conn = DBConnection.connect()) {

            PreparedStatement userStmt =
                    conn.prepareStatement("SELECT user_id FROM users WHERE username=?");
            userStmt.setString(1, user.getUsername());

            ResultSet rs = userStmt.executeQuery();
            if (!rs.next()) return "User not found.";

            int userId = rs.getInt("user_id");

            PreparedStatement stmt = conn.prepareStatement("""
            	    INSERT INTO books(title, author, genre, description, uploaded_by, file_path)
            	    VALUES (?, ?, ?, ?, ?, ?)
            	""");

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, genre);
            stmt.setString(4, desc);
            stmt.setInt(5, userId);
            stmt.setString(6, filePath); //testing filepath to see if it fixes things
            

            stmt.executeUpdate();
            
            SystemLogDAO.logAction(
                    user.getUsername(),
                    "Uploaded book: " + title
            );
            
            return "Content uploaded successfully.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Upload failed.";
        }
    }
    
    public String moderateContent(User user, int bookId, String status) {

        if (!AuthService.hasAccess(user, Role.CONTENT_MODERATOR, Role.ADMIN)) {
            return "Access denied.";
        }

        try (Connection conn = DBConnection.connect()) {

            String query = """
                UPDATE books
                SET status = ?
                WHERE book_id = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, status);
            stmt.setInt(2, bookId);

            int rows = stmt.executeUpdate();

            if(rows > 0){
            	SystemLogDAO.logAction(
                        user.getUsername(),
                        "Moderated book ID: " + bookId +
                        " -> " + status
                );
                return "Content moderated successfully.";
            }

            return "Book not found.";

        } catch(Exception e){
            e.printStackTrace();
            return "Moderation failed.";
        }
    }

    // SEARCH (Someday, someone's going to look at you like you're the one that they were searching for)
    public void searchContent(String keyword) {
        try (Connection conn = DBConnection.connect()) {

            String query = """
                SELECT title, author, genre, description
                FROM books
                WHERE LOWER(title) LIKE LOWER(?)
                   OR LOWER(author) LIKE LOWER(?)
                   OR LOWER(genre) LIKE LOWER(?)
            """;

            PreparedStatement stmt = conn.prepareStatement(query);

            String key = "%" + keyword + "%";
            stmt.setString(1, key);
            stmt.setString(2, key);
            stmt.setString(3, key);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("\nTitle: " + rs.getString("title"));
                System.out.println("Author: " + rs.getString("author"));
                System.out.println("Genre: " + rs.getString("genre"));
                System.out.println("Description: " + rs.getString("description"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void filterByGenre(String genre) {
        try (Connection conn = DBConnection.connect()) {

            String query = """
                SELECT title, author, genre
                FROM books
                WHERE LOWER(genre) = LOWER(?)
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, genre);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Author: " + rs.getString("author"));
                System.out.println("Genre: " + rs.getString("genre"));
                System.out.println("----------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getBookFilePath(String title) {
        try (Connection conn = DBConnection.connect()) {

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT file_path FROM books WHERE LOWER(title) = LOWER(?)"
            );

            stmt.setString(1, title);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("file_path");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    } 
}