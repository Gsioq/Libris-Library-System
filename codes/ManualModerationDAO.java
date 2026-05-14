package application;

import java.sql.*;
import database.DBConnection;

public class ManualModerationDAO {

	public static boolean approveDiscussion(int discussionId) {
	    try(Connection conn = DBConnection.connect()) {

	        String sql = """
	            UPDATE discussions
	            SET status='APPROVED'
	            WHERE discussion_id=? 
	            AND status='PENDING'
	        """;

	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, discussionId);

	        int rowsUpdated = stmt.executeUpdate();

	        return rowsUpdated > 0;

	    } catch(Exception e){
	        e.printStackTrace();
	    }

	    return false;
	}

    public static void flagDiscussion(int discussionId) {
        updateStatus("discussions", discussionId, "FLAGGED");
    }

    public static boolean approveReview(int reviewId) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                UPDATE reviews
                SET status='APPROVED'
                WHERE review_id=?
            	AND status IN ('PENDING', 'PENDING_REVIEW')
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reviewId);

            int rowsUpdated = stmt.executeUpdate();

            return rowsUpdated > 0;

        } catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public static boolean flagReview(int reviewId) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                UPDATE reviews
                SET status='FLAGGED'
                WHERE review_id=?
                AND status IN ('PENDING', 'PENDING_REVIEW')
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reviewId);

            int rowsUpdated = stmt.executeUpdate();

            return rowsUpdated > 0;

        } catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    private static void updateStatus(String table, int id, String status) {
        try(Connection conn = DBConnection.connect()) {

            String idColumn;

            if(table.equals("reviews")) {
                idColumn = "review_id";
            } 
            else if(table.equals("discussions")) {
                idColumn = "discussion_id";
            } 
            else {
                throw new RuntimeException("Unknown table: " + table);
            }

            String sql = "UPDATE " + table +
                         " SET status=? WHERE " + idColumn + "=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, id);

            stmt.executeUpdate();

        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static String getPendingDiscussions() {
        StringBuilder result = new StringBuilder();

        try(Connection conn = DBConnection.connect()) {

        	String sql = """
        		    SELECT discussion_id, username, title, content
        		    FROM discussions
        		    WHERE status='PENDING'
        		    ORDER BY created_at DESC
        		""";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                result.append("Discussion ID: ")
                      .append(rs.getInt("discussion_id"))
                      .append("\nUser: ")
                      .append(rs.getString("username"))
                      .append("\nTitle: ")
                      .append(rs.getString("title"))
                      .append("\nContent: ")
                      .append(rs.getString("content"))
                      .append("\n-------------------\n");
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return result.toString();
    }
    
    public static String getPendingReviews() {
        StringBuilder result = new StringBuilder();

        try(Connection conn = DBConnection.connect()) {

            String sql = """
                SELECT review_id, username, book_title, rating, review_text
                FROM reviews
                WHERE status IN ('PENDING', 'PENDING_REVIEW')
                ORDER BY review_id ASC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                result.append("Review ID: ")
                      .append(rs.getInt("review_id"))
                      .append("\nUser: ")
                      .append(rs.getString("username"))
                      .append("\nBook: ")
                      .append(rs.getString("book_title"))
                      .append("\nRating: ")
                      .append(rs.getInt("rating"))
                      .append("/5")
                      .append("\nReview: ")
                      .append(rs.getString("review_text"))
                      .append("\n------------------------\n");
            }

            if(result.length() == 0){
                return "No pending reviews.";
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return result.toString();
    }
    
    public static String getNotifications() {
        StringBuilder result = new StringBuilder();

        try(Connection conn = DBConnection.connect()) {

            String sql = """
                SELECT message
                FROM moderation_notifications
                WHERE is_read = false
                ORDER BY created_at DESC
            """;

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                result.append("🔔 ")
                      .append(rs.getString("message"))
                      .append("\n");
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return result.toString();
    }
}