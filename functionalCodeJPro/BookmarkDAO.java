package application;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.DBConnection;
import java.util.*;

public class BookmarkDAO {
	public static void saveBookmark(String username, Bookmark bookmark) {
	    String getActualTitleSql = """
	        SELECT title
	        FROM books
	        WHERE LOWER(title) LIKE LOWER(?)
	        LIMIT 1
	    """;

	    String insertSql = """
	        INSERT INTO bookmarks (username, book_title, page_number)
	        VALUES (?, ?, ?)
	    """;

	    try (
	        Connection conn = DBConnection.getConnection()
	    ) {
	        String actualTitle = bookmark.getBookTitle();

	        PreparedStatement titleStmt =
	                conn.prepareStatement(getActualTitleSql);

	        titleStmt.setString(
	                1,
	                "%" + bookmark.getBookTitle() + "%"
	        );

	        ResultSet rs = titleStmt.executeQuery();

	        if (rs.next()) {
	            actualTitle = rs.getString("title");
	        }

	        PreparedStatement insertStmt =
	                conn.prepareStatement(insertSql);

	        insertStmt.setString(1, username);
	        insertStmt.setString(2, actualTitle);
	        insertStmt.setInt(3, bookmark.getPageNumber());

	        insertStmt.executeUpdate();

	        System.out.println("Bookmark saved successfully!");

	    } catch (Exception e) {
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
	
	public static List<Bookmark> getAllBookmarks(String username) {
	    List<Bookmark> bookmarks = new ArrayList<>();

	    String sql = """
	        SELECT b1.book_title, b1.page_number
	        FROM bookmarks b1
	        INNER JOIN (
	            SELECT book_title, MAX(id) as latest_id
	            FROM bookmarks
	            WHERE username = ?
	            GROUP BY book_title
	        ) b2
	        ON b1.id = b2.latest_id
	        ORDER BY b1.id DESC
	    """;

	    try (
	        Connection conn = DBConnection.connect();
	        PreparedStatement stmt = conn.prepareStatement(sql)
	    ) {
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            bookmarks.add(
	                new Bookmark(
	                    rs.getString("book_title"),
	                    rs.getInt("page_number")
	                )
	            );
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return bookmarks;
	}
	
	public static void deleteBookmark(
	        String username,
	        String bookTitle
	) {
	    String sql = """
	        DELETE FROM bookmarks
	        WHERE username = ?
	        AND LOWER(book_title) = LOWER(?)
	    """;

	    try (
	        Connection conn = DBConnection.connect();
	        PreparedStatement stmt = conn.prepareStatement(sql)
	    ) {
	        stmt.setString(1, username);
	        stmt.setString(2, bookTitle);

	        stmt.executeUpdate();

	        System.out.println("Bookmark deleted.");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}