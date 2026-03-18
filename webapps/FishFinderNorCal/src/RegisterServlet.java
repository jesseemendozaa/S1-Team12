import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Register");
        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Server-side validation
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "All fields are required.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("pageTitle", "Register");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("pageTitle", "Register");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Users (username, email, password_hash) VALUES (?, ?, ?)");
            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, hashedPassword);
            ps.executeUpdate();

            HttpSession session = request.getSession();
            session.setAttribute("successMessage", "Registration successful! Please login.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (SQLIntegrityConstraintViolationException e) {
            request.setAttribute("error", "Username or email already exists.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("pageTitle", "Register");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
