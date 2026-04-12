import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/admin/users")
public class AdminUserManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"moderator".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<Map<String, Object>> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.user_id, u.username, u.email, u.account_status, u.created_at, " +
                "m.user_id AS moderator_id, " +
                "b.ban_id, b.reason, b.ban_end, b.is_active " +
                "FROM Users u " +
                "LEFT JOIN Moderators m ON u.user_id = m.user_id " +
                "LEFT JOIN Bans b ON u.user_id = b.target_user_id AND b.is_active = 1 " +
                "ORDER BY u.created_at DESC, u.user_id DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("userId", rs.getInt("user_id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("status", rs.getString("account_status"));
                user.put("createdAt", rs.getTimestamp("created_at"));
                user.put("isModerator", rs.getObject("moderator_id") != null);
                user.put("activeBanId", rs.getObject("ban_id"));
                user.put("banReason", rs.getString("reason"));
                user.put("banEnd", rs.getTimestamp("ban_end"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("users", users);
        request.setAttribute("pageTitle", "Manage Users");
        request.getRequestDispatcher("/WEB-INF/jsp/adminUsers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"moderator".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int moderatorUserId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        String targetUserIdStr = request.getParameter("targetUserId");
        if (targetUserIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        int targetUserId = Integer.parseInt(targetUserIdStr);
        if (targetUserId == moderatorUserId) {
            session.setAttribute("errorMessage", "You cannot ban your own account.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            if ("unban".equals(action)) {
                PreparedStatement endBanPs = conn.prepareStatement(
                    "UPDATE Bans SET is_active = 0, ban_end = COALESCE(ban_end, CURRENT_TIMESTAMP) " +
                    "WHERE target_user_id = ? AND is_active = 1");
                endBanPs.setInt(1, targetUserId);
                endBanPs.executeUpdate();

                PreparedStatement userPs = conn.prepareStatement(
                    "UPDATE Users SET account_status = 'active' WHERE user_id = ? AND account_status = 'banned'");
                userPs.setInt(1, targetUserId);
                userPs.executeUpdate();
                session.setAttribute("successMessage", "User ban removed.");
            } else {
                String reason = request.getParameter("reason");
                String banEnd = request.getParameter("banEnd");
                if (reason == null || reason.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "Ban reason is required.");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                PreparedStatement deactivateExistingPs = conn.prepareStatement(
                    "UPDATE Bans SET is_active = 0 WHERE target_user_id = ? AND is_active = 1");
                deactivateExistingPs.setInt(1, targetUserId);
                deactivateExistingPs.executeUpdate();

                PreparedStatement insertBanPs = conn.prepareStatement(
                    "INSERT INTO Bans (moderator_user_id, target_user_id, reason, ban_end, is_active) " +
                    "VALUES (?, ?, ?, ?, 1)");
                insertBanPs.setInt(1, moderatorUserId);
                insertBanPs.setInt(2, targetUserId);
                insertBanPs.setString(3, reason.trim());
                if (banEnd != null && !banEnd.trim().isEmpty()) {
                    insertBanPs.setTimestamp(4, Timestamp.valueOf(banEnd.trim().replace("T", " ") + ":00"));
                } else {
                    insertBanPs.setNull(4, Types.TIMESTAMP);
                }
                insertBanPs.executeUpdate();

                PreparedStatement userPs = conn.prepareStatement(
                    "UPDATE Users SET account_status = 'banned' WHERE user_id = ?");
                userPs.setInt(1, targetUserId);
                userPs.executeUpdate();
                session.setAttribute("successMessage", "User banned successfully.");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
