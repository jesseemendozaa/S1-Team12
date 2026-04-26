import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/locations")
public class LocationListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        List<Map<String, Object>> locations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT DISTINCT l.location_id, l.location_name, l.description, l.city, " +
                         "lt.type_name " +
                         "FROM Locations l " +
                         "LEFT JOIN LocationTypes lt ON l.type_id = lt.type_id " +
                         "LEFT JOIN LocationSpecies ls ON l.location_id = ls.location_id " +
                         "LEFT JOIN Species s ON ls.species_id = s.species_id";
            if (search != null && !search.trim().isEmpty()) {
                sql += " WHERE l.location_name LIKE ? OR l.city LIKE ? OR l.address LIKE ? " +
                       "OR s.common_name LIKE ? OR lt.type_name LIKE ?";
            }
            sql += " ORDER BY l.location_name";

            PreparedStatement ps = conn.prepareStatement(sql);
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
                ps.setString(4, pattern);
                ps.setString(5, pattern);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> loc = new HashMap<>();
                loc.put("locationId", rs.getInt("location_id"));
                loc.put("name", rs.getString("location_name"));
                loc.put("description", rs.getString("description"));
                loc.put("region", rs.getString("city"));
                loc.put("typeName", rs.getString("type_name"));
                locations.add(loc);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("locations", locations);
        request.setAttribute("search", search);
        request.setAttribute("pageTitle", "Fishing Locations");
        request.getRequestDispatcher("/WEB-INF/jsp/locations.jsp").forward(request, response);
    }
}
