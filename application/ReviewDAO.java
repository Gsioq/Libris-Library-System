package application;
import java.sql.*;
import database.DBConnection;

public class ReviewDAO {

    public static void createReview(Review r){
        try(Connection conn = DBConnection.connect()){

            String status =
                AutoModerator.isAppropriate(r.getReviewText())
                ? "APPROVED"
                : "PENDING";

            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO reviews
                (username,book_title,rating,review_text,status)
                VALUES(?,?,?,?,?)
            """);

            stmt.setString(1, r.getUsername());
            stmt.setString(2, r.getBookTitle());
            stmt.setInt(3, r.getRating());
            stmt.setString(4, r.getReviewText());
            stmt.setString(5, status);

            stmt.executeUpdate();

            SystemLogDAO.logAction(
                r.getUsername(),
                "Reviewed book: " + r.getBookTitle()
            );

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
