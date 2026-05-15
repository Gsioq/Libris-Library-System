package application;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class ModerationNotificationDAO {

    public static void createNotification(
            String contentType,
            String message
    ) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                INSERT INTO moderation_notifications
                (content_type, message, is_read)
                VALUES (?, ?, false)
            """;

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setString(1, contentType);
            stmt.setString(2, message);

            stmt.executeUpdate();

            System.out.println("Moderator notification created.");

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}