import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/reports")
public class CatchReportListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String locationIdStr = request.getParameter("locationId");
        String userIdStr = request.getParameter("userId");
        List<Map<String, Object>> reports = new ArrayList<>();

        HttpSession session = request.getSession(false);

        try (Connection conn = DBUtil.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT cr.report_id, cr.catch_date, cr.weight, cr.size, cr.time_of_day, " +
                "s.common_name as species_name, l.location_name, l.location_id, " +
                "u.username, cr.user_id " +
                "FROM CatchReports cr " +
                "JOIN Species s ON cr.species_id = s.species_id " +
                "JOIN Locations l ON cr.location_id = l.location_id " +
                "LEFT JOIN Users u ON cr.user_id = u.user_id WHERE 1=1");

            List<Object> params = new ArrayList<>();

            if (locationIdStr != null && !locationIdStr.trim().isEmpty()) {
                sql.append(" AND cr.location_id = ?");
                params.add(Integer.parseInt(locationIdStr));
            }
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                if ("mine".equals(userIdStr) && session != null && session.getAttribute("userId") != null) {
                    sql.append(" AND cr.user_id = ?");
                    params.add((int) session.getAttribute("userId"));
                } else {
                    try {
                        sql.append(" AND cr.user_id = ?");
                        params.add(Integer.parseInt(userIdStr));
                    } catch (NumberFormatException ignored) {}
                }
            }

            sql.append(" ORDER BY cr.catch_date DESC, cr.created_at DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    ps.setInt(i + 1, (Integer) params.get(i));
                } else {
                    ps.setString(i + 1, params.get(i).toString());
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("reportId", rs.getInt("report_id"));
                r.put("catchDate", rs.getString("catch_date"));
                r.put("weightLbs", rs.getBigDecimal("weight"));
                r.put("lengthInches", rs.getBigDecimal("size"));
                r.put("method", rs.getString("time_of_day"));
                r.put("speciesName", rs.getString("species_name"));
                r.put("locationName", rs.getString("location_name"));
                r.put("locationId", rs.getInt("location_id"));
                r.put("username", rs.getString("username"));
                r.put("userId", rs.getObject("user_id"));
                reports.add(r);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("reports", reports);
        request.setAttribute("pageTitle", "Catch Reports");
        request.getRequestDispatcher("/WEB-INF/jsp/reports.jsp").forward(request, response);
    }
}
