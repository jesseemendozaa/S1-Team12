import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/location")
public class LocationDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        int locationId;
        try {
            locationId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Get location details
            PreparedStatement ps = conn.prepareStatement(
                "SELECT l.*, lt.type_name, u.username as creator_name " +
                "FROM Locations l " +
                "LEFT JOIN LocationTypes lt ON l.type_id = lt.type_id " +
                "LEFT JOIN Users u ON l.created_by = u.user_id " +
                "WHERE l.location_id = ?");
            ps.setInt(1, locationId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendRedirect(request.getContextPath() + "/locations");
                return;
            }

            Map<String, Object> location = new HashMap<>();
            location.put("locationId", rs.getInt("location_id"));
            location.put("name", rs.getString("name"));
            location.put("description", rs.getString("description"));
            location.put("region", rs.getString("region"));
            location.put("latitude", rs.getBigDecimal("latitude"));
            location.put("longitude", rs.getBigDecimal("longitude"));
            location.put("typeName", rs.getString("type_name"));
            location.put("creatorName", rs.getString("creator_name"));
            location.put("createdAt", rs.getTimestamp("created_at"));
            request.setAttribute("location", location);

            // Get species at this location
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT s.species_id, s.common_name, s.scientific_name, ls.evidence_count, ls.last_reported " +
                "FROM LocationSpecies ls " +
                "JOIN Species s ON ls.species_id = s.species_id " +
                "WHERE ls.location_id = ? ORDER BY s.common_name");
            ps2.setInt(1, locationId);
            ResultSet rs2 = ps2.executeQuery();
            List<Map<String, Object>> species = new ArrayList<>();
            while (rs2.next()) {
                Map<String, Object> sp = new HashMap<>();
                sp.put("speciesId", rs2.getInt("species_id"));
                sp.put("commonName", rs2.getString("common_name"));
                sp.put("scientificName", rs2.getString("scientific_name"));
                sp.put("evidenceCount", rs2.getInt("evidence_count"));
                sp.put("lastReported", rs2.getDate("last_reported"));
                species.add(sp);
            }
            request.setAttribute("species", species);

            // Get catch reports at this location
            PreparedStatement ps3 = conn.prepareStatement(
                "SELECT cr.report_id, cr.catch_date, cr.weight_lbs, cr.length_inches, cr.method, " +
                "s.common_name, u.username " +
                "FROM CatchReports cr " +
                "JOIN Species s ON cr.species_id = s.species_id " +
                "LEFT JOIN Users u ON cr.user_id = u.user_id " +
                "WHERE cr.location_id = ? ORDER BY cr.catch_date DESC LIMIT 10");
            ps3.setInt(1, locationId);
            ResultSet rs3 = ps3.executeQuery();
            List<Map<String, Object>> reports = new ArrayList<>();
            while (rs3.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("reportId", rs3.getInt("report_id"));
                r.put("catchDate", rs3.getString("catch_date"));
                r.put("weightLbs", rs3.getBigDecimal("weight_lbs"));
                r.put("lengthInches", rs3.getBigDecimal("length_inches"));
                r.put("method", rs3.getString("method"));
                r.put("speciesName", rs3.getString("common_name"));
                r.put("username", rs3.getString("username"));
                reports.add(r);
            }
            request.setAttribute("reports", reports);

            // Check if user has favorited this location
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                int userId = (int) session.getAttribute("userId");
                PreparedStatement ps4 = conn.prepareStatement(
                    "SELECT 1 FROM Favorites WHERE user_id = ? AND location_id = ?");
                ps4.setInt(1, userId);
                ps4.setInt(2, locationId);
                ResultSet rs4 = ps4.executeQuery();
                request.setAttribute("isFavorited", rs4.next());
            }

        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("pageTitle", "Location Details");
        request.getRequestDispatcher("/WEB-INF/jsp/locationDetail.jsp").forward(request, response);
    }
}
