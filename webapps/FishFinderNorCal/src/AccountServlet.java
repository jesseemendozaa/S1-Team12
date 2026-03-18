import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT username, email, account_status, created_at FROM Users WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                request.setAttribute("userUsername", rs.getString("username"));
                request.setAttribute("userEmail", rs.getString("email"));
                request.setAttribute("userStatus", rs.getString("account_status"));
                request.setAttribute("userCreatedAt", rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("pageTitle", "My Account");
        request.getRequestDispatcher("/WEB-INF/jsp/account.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try (Connection conn = DBUtil.getConnection()) {
            if ("delete".equals(action)) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Users SET account_status = 'deleted' WHERE user_id = ?");
                ps.setInt(1, userId);
                ps.executeUpdate();
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/home.html");
                return;
            }

            // Update profile
            String newUsername = request.getParameter("username");
            String newEmail = request.getParameter("email");
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");

            // Verify current password if changing password
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "Current password is required to set a new password.");
                    response.sendRedirect(request.getContextPath() + "/account");
                    return;
                }

                PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT password_hash FROM Users WHERE user_id = ?");
                checkPs.setInt(1, userId);
                ResultSet checkRs = checkPs.executeQuery();
                if (checkRs.next()) {
                    String storedHash = checkRs.getString("password_hash");
                    if (!storedHash.equals(PasswordUtil.hashPassword(currentPassword))) {
                        session.setAttribute("errorMessage", "Current password is incorrect.");
                        response.sendRedirect(request.getContextPath() + "/account");
                        return;
                    }
                }

                // Update password
                PreparedStatement pwPs = conn.prepareStatement(
                    "UPDATE Users SET password_hash = ? WHERE user_id = ?");
                pwPs.setString(1, PasswordUtil.hashPassword(newPassword));
                pwPs.setInt(2, userId);
                pwPs.executeUpdate();
            }

            // Update username and email
            if (newUsername != null && !newUsername.trim().isEmpty() &&
                newEmail != null && !newEmail.trim().isEmpty()) {
                PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE Users SET username = ?, email = ? WHERE user_id = ?");
                updatePs.setString(1, newUsername.trim());
                updatePs.setString(2, newEmail.trim());
                updatePs.setInt(3, userId);
                updatePs.executeUpdate();
                session.setAttribute("username", newUsername.trim());
            }

            session.setAttribute("successMessage", "Account updated successfully!");
        } catch (SQLIntegrityConstraintViolationException e) {
            session.setAttribute("errorMessage", "Username or email already taken.");
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        response.sendRedirect(request.getContextPath() + "/account");
    }
}
