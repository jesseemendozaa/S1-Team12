import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/report/edit")
public class CatchReportEditServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/reports");
            return;
        }

        int reportId;
        try {
            reportId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/reports");
            return;
        }

        boolean editMode = "true".equals(request.getParameter("edit"));

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT cr.*, s.common_name as species_name, s.species_id, " +
                "l.location_name, l.location_id, u.username " +
                "FROM CatchReports cr " +
                "JOIN Species s ON cr.species_id = s.species_id " +
                "JOIN Locations l ON cr.location_id = l.location_id " +
                "LEFT JOIN Users u ON cr.user_id = u.user_id " +
                "WHERE cr.report_id = ?");
            ps.setInt(1, reportId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendRedirect(request.getContextPath() + "/reports");
                return;
            }

            Map<String, Object> report = new HashMap<>();
            report.put("reportId", rs.getInt("report_id"));
            report.put("userId", rs.getObject("user_id"));
            report.put("locationId", rs.getInt("location_id"));
            report.put("speciesId", rs.getInt("species_id"));
            report.put("catchDate", rs.getString("catch_date"));
            report.put("weightLbs", rs.getBigDecimal("weight"));
            report.put("lengthInches", rs.getBigDecimal("size"));
            report.put("method", rs.getString("time_of_day"));
            report.put("notes", rs.getString("notes"));
            report.put("createdAt", rs.getTimestamp("created_at"));
            report.put("speciesName", rs.getString("species_name"));
            report.put("locationName", rs.getString("location_name"));
            report.put("username", rs.getString("username"));
            request.setAttribute("report", report);

            // Check ownership
            HttpSession session = request.getSession(false);
            boolean isOwner = false;
            boolean isMod = false;
            if (session != null && session.getAttribute("userId") != null) {
                int sessionUserId = (int) session.getAttribute("userId");
                Object reportUserId = rs.getObject("user_id");
                isOwner = reportUserId != null && sessionUserId == ((Number) reportUserId).intValue();
                isMod = "moderator".equals(session.getAttribute("role"));
            }
            request.setAttribute("isOwner", isOwner);
            request.setAttribute("isMod", isMod);

            if (editMode && (isOwner || isMod)) {
                // Load dropdowns for edit form
                loadDropdowns(request, conn);
                request.setAttribute("editMode", true);
                request.setAttribute("pageTitle", "Edit Report");
                request.getRequestDispatcher("/WEB-INF/jsp/reportForm.jsp").forward(request, response);
            } else {
                // Load comments for detail view
                PreparedStatement cps = conn.prepareStatement(
                    "SELECT c.comment_id, c.comment_text, c.commented_at, u.username " +
                    "FROM Comments c LEFT JOIN Users u ON c.user_id = u.user_id " +
                    "WHERE c.report_id = ? ORDER BY c.commented_at ASC");
                cps.setInt(1, reportId);
                ResultSet crs = cps.executeQuery();
                List<Map<String, Object>> comments = new ArrayList<>();
                while (crs.next()) {
                    Map<String, Object> c = new HashMap<>();
                    c.put("commentId", crs.getInt("comment_id"));
                    c.put("commentText", crs.getString("comment_text"));
                    c.put("createdAt", crs.getTimestamp("commented_at"));
                    c.put("username", crs.getString("username"));
                    comments.add(c);
                }
                request.setAttribute("comments", comments);

                request.setAttribute("pageTitle", "Report Details");
                request.getRequestDispatcher("/WEB-INF/jsp/reportDetail.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int sessionUserId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        String idStr = request.getParameter("reportId");

        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/reports");
            return;
        }

        int reportId = Integer.parseInt(idStr);

        try (Connection conn = DBUtil.getConnection()) {
            // Verify ownership or mod
            PreparedStatement checkPs = conn.prepareStatement("SELECT user_id FROM CatchReports WHERE report_id = ?");
            checkPs.setInt(1, reportId);
            ResultSet checkRs = checkPs.executeQuery();
            if (!checkRs.next()) {
                response.sendRedirect(request.getContextPath() + "/reports");
                return;
            }
            Object reportOwner = checkRs.getObject("user_id");
            boolean isOwner = reportOwner != null && sessionUserId == ((Number) reportOwner).intValue();
            boolean isMod = "moderator".equals(session.getAttribute("role"));

            if (!isOwner && !isMod) {
                session.setAttribute("errorMessage", "You don't have permission to modify this report.");
                response.sendRedirect(request.getContextPath() + "/report/edit?id=" + reportId);
                return;
            }

            if ("delete".equals(action)) {
                PreparedStatement delPs = conn.prepareStatement("DELETE FROM CatchReports WHERE report_id = ?");
                delPs.setInt(1, reportId);
                delPs.executeUpdate();
                session.setAttribute("successMessage", "Report deleted.");
                response.sendRedirect(request.getContextPath() + "/reports");
            } else {
                // Update
                String locationIdStr = request.getParameter("locationId");
                String speciesIdStr = request.getParameter("speciesId");
                String catchDate = request.getParameter("catchDate");
                String weightStr = request.getParameter("weightLbs");
                String lengthStr = request.getParameter("lengthInches");
                String method = request.getParameter("method");
                String notes = request.getParameter("notes");

                PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE CatchReports SET location_id=?, species_id=?, catch_date=?, " +
                    "weight=?, size=?, time_of_day=?, notes=? WHERE report_id=?");
                updatePs.setInt(1, Integer.parseInt(locationIdStr));
                updatePs.setInt(2, Integer.parseInt(speciesIdStr));
                updatePs.setDate(3, java.sql.Date.valueOf(catchDate));

                if (weightStr != null && !weightStr.trim().isEmpty()) {
                    updatePs.setBigDecimal(4, new java.math.BigDecimal(weightStr.trim()));
                } else {
                    updatePs.setNull(4, Types.DECIMAL);
                }
                if (lengthStr != null && !lengthStr.trim().isEmpty()) {
                    updatePs.setBigDecimal(5, new java.math.BigDecimal(lengthStr.trim()));
                } else {
                    updatePs.setNull(5, Types.DECIMAL);
                }
                if (method != null && !method.trim().isEmpty()) {
                    updatePs.setTime(6, java.sql.Time.valueOf(method.trim() + ":00"));
                } else {
                    updatePs.setNull(6, Types.TIME);
                }
                updatePs.setString(7, notes != null ? notes.trim() : null);
                updatePs.setInt(8, reportId);
                updatePs.executeUpdate();

                session.setAttribute("successMessage", "Report updated.");
                response.sendRedirect(request.getContextPath() + "/report/edit?id=" + reportId);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void loadDropdowns(HttpServletRequest request, Connection conn) throws SQLException {
        List<Map<String, Object>> locations = new ArrayList<>();
        PreparedStatement ps1 = conn.prepareStatement("SELECT location_id, location_name FROM Locations ORDER BY location_name");
        ResultSet rs1 = ps1.executeQuery();
        while (rs1.next()) {
            Map<String, Object> m = new HashMap<>();
            m.put("locationId", rs1.getInt("location_id"));
            m.put("name", rs1.getString("location_name"));
            locations.add(m);
        }
        request.setAttribute("locations", locations);

        List<Map<String, Object>> species = new ArrayList<>();
        PreparedStatement ps2 = conn.prepareStatement("SELECT species_id, common_name FROM Species ORDER BY common_name");
        ResultSet rs2 = ps2.executeQuery();
        while (rs2.next()) {
            Map<String, Object> m = new HashMap<>();
            m.put("speciesId", rs2.getInt("species_id"));
            m.put("commonName", rs2.getString("common_name"));
            species.add(m);
        }
        request.setAttribute("species", species);
    }
}
