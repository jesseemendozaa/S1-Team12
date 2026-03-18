import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Login");
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Username and password are required.");
            request.setAttribute("pageTitle", "Login");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT user_id, username, account_status FROM Users WHERE username = ? AND password_hash = ?");
            ps.setString(1, username.trim());
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String status = rs.getString("account_status");
                if ("suspended".equals(status)) {
                    request.setAttribute("error", "Your account has been suspended. Contact a moderator.");
                    request.setAttribute("pageTitle", "Login");
                    request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                    return;
                }
                if ("deleted".equals(status)) {
                    request.setAttribute("error", "This account has been deleted.");
                    request.setAttribute("pageTitle", "Login");
                    request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                    return;
                }

                int userId = rs.getInt("user_id");
                String uname = rs.getString("username");

                // Check if moderator
                String role = "user";
                PreparedStatement modPs = conn.prepareStatement(
                    "SELECT user_id FROM Moderators WHERE user_id = ?");
                modPs.setInt(1, userId);
                ResultSet modRs = modPs.executeQuery();
                if (modRs.next()) {
                    role = "moderator";
                }

                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                session.setAttribute("username", uname);
                session.setAttribute("role", role);

                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                request.setAttribute("error", "Invalid username or password.");
                request.setAttribute("pageTitle", "Login");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
