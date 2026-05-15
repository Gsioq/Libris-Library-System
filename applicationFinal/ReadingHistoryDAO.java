package application;

import java.sql.*;
import database.DBConnection;

public class ReadingHistoryDAO {

    public static int getUniqueBooksRead(int userId) {
        String sql = """
            SELECT COUNT(DISTINCT book_title)
            FROM user_read_history
            WHERE user_id = ?
        """;

        try (
            Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static boolean hasOpenedBook(
            int userId,
            String bookTitle
    ) {
        String sql = """
            SELECT *
            FROM user_read_history
            WHERE user_id = ?
            AND LOWER(book_title) = LOWER(?)
        """;

        try (
            Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.setString(2, bookTitle);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void logBookOpen(
            int userId,
            String bookTitle
    ) {
        String sql = """
            INSERT INTO user_read_history(user_id, book_title)
            VALUES (?, ?)
        """;

        try (
            Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.setString(2, bookTitle);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}