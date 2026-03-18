import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/favorite")
public class FavoriteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String locationIdStr = request.getParameter("locationId");

        if (locationIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        int locationId = Integer.parseInt(locationIdStr);

        try (Connection conn = DBUtil.getConnection()) {
            // Check if already favorited
            PreparedStatement checkPs = conn.prepareStatement(
                "SELECT 1 FROM Favorites WHERE user_id = ? AND location_id = ?");
            checkPs.setInt(1, userId);
            checkPs.setInt(2, locationId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Remove favorite
                PreparedStatement delPs = conn.prepareStatement(
                    "DELETE FROM Favorites WHERE user_id = ? AND location_id = ?");
                delPs.setInt(1, userId);
                delPs.setInt(2, locationId);
                delPs.executeUpdate();
                session.setAttribute("successMessage", "Removed from favorites.");
            } else {
                // Add favorite
                PreparedStatement insPs = conn.prepareStatement(
                    "INSERT INTO Favorites (user_id, location_id) VALUES (?, ?)");
                insPs.setInt(1, userId);
                insPs.setInt(2, locationId);
                insPs.executeUpdate();
                session.setAttribute("successMessage", "Added to favorites!");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        response.sendRedirect(request.getContextPath() + "/location?id=" + locationId);
    }
}
