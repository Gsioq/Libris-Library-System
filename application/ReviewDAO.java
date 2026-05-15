package application;

import java.sql.*;

public class ReviewDAO {

    public static void createReview(Review r){
        try(Connection conn = DBConnection.connect()){

        	String status =
        		    AutoModerator.isAppropriate(r.getReviewText())
        		    ? "PENDING_REVIEW"
        		    : "FLAGGED";

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

            ModerationNotificationDAO.createNotification(
                    "REVIEW",
                    "New review submitted for: " + r.getBookTitle()
            );

        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static String getApprovedReviews() {
        StringBuilder result = new StringBuilder();

        try(Connection conn = DBConnection.connect()) {

            String sql = """
                SELECT username, book_title, rating, review_text
                FROM reviews
                WHERE status = 'APPROVED'
                ORDER BY review_id DESC
            """;

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                result.append("User: ")
                      .append(rs.getString("username"))
                      .append("\nBook: ")
                      .append(rs.getString("book_title"))
                      .append("\nRating: ")
                      .append(rs.getInt("rating"))
                      .append("/5\nReview: ")
                      .append(rs.getString("review_text"))
                      .append("\n------------------------\n");
            }

            if(result.length() == 0){
                return "No approved reviews yet.";
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return result.toString();
    }
}