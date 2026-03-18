import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String reportIdStr = request.getParameter("reportId");
        String commentText = request.getParameter("commentText");

        if (reportIdStr == null || commentText == null || commentText.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/reports");
            return;
        }

        int reportId = Integer.parseInt(reportIdStr);

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Comments (report_id, user_id, comment_text) VALUES (?, ?, ?)");
            ps.setInt(1, reportId);
            ps.setInt(2, userId);
            ps.setString(3, commentText.trim());
            ps.executeUpdate();

            session.setAttribute("successMessage", "Comment added!");
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        response.sendRedirect(request.getContextPath() + "/report/edit?id=" + reportId);
    }
}
