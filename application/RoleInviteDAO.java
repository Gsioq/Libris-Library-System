package application;

import java.sql.*;

public class RoleInviteDAO {

    public static void createInvite(
            String email,
            String role,
            String code
    ) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                INSERT INTO role_invites
                (email, role, invite_code)
                VALUES (?, ?, ?)
            """;

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setString(1, email);
            stmt.setString(2, role);
            stmt.setString(3, code);

            stmt.executeUpdate();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean validateInvite(
            String email,
            String role,
            String code
    ) {
        try(Connection conn = DBConnection.connect()) {

            String sql = """
                SELECT *
                FROM role_invites
                WHERE email=?
                AND role=?
                AND invite_code=?
                AND status='PENDING'
            """;

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setString(1, email);
            stmt.setString(2, role);
            stmt.setString(3, code);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
}