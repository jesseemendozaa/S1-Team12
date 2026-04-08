import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/favorites")
public class FavoriteListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        List<Map<String, Object>> favorites = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT l.location_id, l.name, l.region, lt.type_name, f.created_at " +
                "FROM Favorites f " +
                "JOIN Locations l ON f.location_id = l.location_id " +
                "LEFT JOIN LocationTypes lt ON l.type_id = lt.type_id " +
                "WHERE f.user_id = ? ORDER BY f.created_at DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fav = new HashMap<>();
                fav.put("locationId", rs.getInt("location_id"));
                fav.put("name", rs.getString("name"));
                fav.put("region", rs.getString("region"));
                fav.put("typeName", rs.getString("type_name"));
                fav.put("favoritedAt", rs.getTimestamp("created_at"));
                favorites.add(fav);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("favorites", favorites);
        request.setAttribute("pageTitle", "My Favorites");
        request.getRequestDispatcher("/WEB-INF/jsp/favorites.jsp").forward(request, response);
    }
}
