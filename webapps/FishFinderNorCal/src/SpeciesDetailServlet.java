import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/species")
public class SpeciesDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        int speciesId;
        try {
            speciesId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT species_id, common_name, scientific_name " +
                "FROM Species WHERE species_id = ?");
            ps.setInt(1, speciesId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendRedirect(request.getContextPath() + "/locations");
                return;
            }

            Map<String, Object> species = new HashMap<>();
            species.put("speciesId", rs.getInt("species_id"));
            species.put("commonName", rs.getString("common_name"));
            species.put("scientificName", rs.getString("scientific_name"));
            request.setAttribute("species", species);

            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT l.location_id, l.location_name, l.city, lt.type_name, ls.evidence_count " +
                "FROM LocationSpecies ls " +
                "JOIN Locations l ON ls.location_id = l.location_id " +
                "LEFT JOIN LocationTypes lt ON l.type_id = lt.type_id " +
                "WHERE ls.species_id = ? " +
                "ORDER BY ls.evidence_count DESC, l.location_name");
            ps2.setInt(1, speciesId);
            ResultSet rs2 = ps2.executeQuery();
            List<Map<String, Object>> locations = new ArrayList<>();
            while (rs2.next()) {
                Map<String, Object> location = new HashMap<>();
                location.put("locationId", rs2.getInt("location_id"));
                location.put("name", rs2.getString("location_name"));
                location.put("city", rs2.getString("city"));
                location.put("typeName", rs2.getString("type_name"));
                location.put("evidenceCount", rs2.getInt("evidence_count"));
                locations.add(location);
            }
            request.setAttribute("locations", locations);

            PreparedStatement ps3 = conn.prepareStatement(
                "SELECT cr.report_id, cr.catch_date, cr.weight, cr.size, cr.time_of_day, " +
                "l.location_name, l.location_id, u.username " +
                "FROM CatchReports cr " +
                "JOIN Locations l ON cr.location_id = l.location_id " +
                "LEFT JOIN Users u ON cr.user_id = u.user_id " +
                "WHERE cr.species_id = ? " +
                "ORDER BY cr.catch_date DESC, cr.created_at DESC " +
                "LIMIT 10");
            ps3.setInt(1, speciesId);
            ResultSet rs3 = ps3.executeQuery();
            List<Map<String, Object>> reports = new ArrayList<>();
            while (rs3.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs3.getInt("report_id"));
                report.put("catchDate", rs3.getString("catch_date"));
                report.put("weightLbs", rs3.getBigDecimal("weight"));
                report.put("lengthInches", rs3.getBigDecimal("size"));
                report.put("method", rs3.getString("time_of_day"));
                report.put("locationName", rs3.getString("location_name"));
                report.put("locationId", rs3.getInt("location_id"));
                report.put("username", rs3.getString("username"));
                reports.add(report);
            }
            request.setAttribute("reports", reports);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("pageTitle", "Species Details");
        request.getRequestDispatcher("/WEB-INF/jsp/speciesDetail.jsp").forward(request, response);
    }
}
