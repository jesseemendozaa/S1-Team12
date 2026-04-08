import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
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
            // Get user's report count
            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT COUNT(*) as cnt FROM CatchReports WHERE user_id = ?");
            ps1.setInt(1, userId);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            request.setAttribute("reportCount", rs1.getInt("cnt"));

            // Get user's favorite count
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT COUNT(*) as cnt FROM Favorites WHERE user_id = ?");
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            request.setAttribute("favoriteCount", rs2.getInt("cnt"));

            // Get total locations
            PreparedStatement ps3 = conn.prepareStatement("SELECT COUNT(*) as cnt FROM Locations");
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            request.setAttribute("locationCount", rs3.getInt("cnt"));

            // Get total species
            PreparedStatement ps4 = conn.prepareStatement("SELECT COUNT(*) as cnt FROM Species");
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            request.setAttribute("speciesCount", rs4.getInt("cnt"));

            // Get recent reports (last 5)
            PreparedStatement ps5 = conn.prepareStatement(
                "SELECT cr.report_id, cr.catch_date, cr.weight_lbs, cr.length_inches, " +
                "s.common_name, l.name as location_name, u.username " +
                "FROM CatchReports cr " +
                "JOIN Species s ON cr.species_id = s.species_id " +
                "JOIN Locations l ON cr.location_id = l.location_id " +
                "LEFT JOIN Users u ON cr.user_id = u.user_id " +
                "ORDER BY cr.created_at DESC LIMIT 5");
            ResultSet rs5 = ps5.executeQuery();
            java.util.List<java.util.Map<String, Object>> reports = new java.util.ArrayList<>();
            while (rs5.next()) {
                java.util.Map<String, Object> report = new java.util.HashMap<>();
                report.put("reportId", rs5.getInt("report_id"));
                report.put("catchDate", rs5.getString("catch_date"));
                report.put("weightLbs", rs5.getBigDecimal("weight_lbs"));
                report.put("lengthInches", rs5.getBigDecimal("length_inches"));
                report.put("speciesName", rs5.getString("common_name"));
                report.put("locationName", rs5.getString("location_name"));
                report.put("username", rs5.getString("username"));
                reports.add(report);
            }
            request.setAttribute("recentReports", reports);

        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("pageTitle", "Dashboard");
        request.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(request, response);
    }
}
