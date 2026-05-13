package application;
import java.sql.*;
import database.DBConnection;

public class DiscussionDAO {

    public static void createDiscussion(Discussion d) {
        try(Connection conn = DBConnection.connect()) {

            String status =
                AutoModerator.isAppropriate(d.getContent())
                ? "APPROVED"
                : "PENDING";

            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO discussions(username,title,content,status)
                VALUES(?,?,?,?)
            """);

            stmt.setString(1, d.getUsername());
            stmt.setString(2, d.getTitle());
            stmt.setString(3, d.getContent());
            stmt.setString(4, status);

            stmt.executeUpdate();

            SystemLogDAO.logAction(
                d.getUsername(),
                "Created discussion: " + d.getTitle()
            );

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
