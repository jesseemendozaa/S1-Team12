import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/report/new")
public class CatchReportFormServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        loadDropdowns(request);
        // Pre-select location if passed
        String locationId = request.getParameter("locationId");
        if (locationId != null) {
            request.setAttribute("selectedLocationId", locationId);
        }
        request.setAttribute("pageTitle", "Log a Catch");
        request.getRequestDispatcher("/WEB-INF/jsp/reportForm.jsp").forward(request, response);
    }

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
        String speciesIdStr = request.getParameter("speciesId");
        String catchDate = request.getParameter("catchDate");
        String weightStr = request.getParameter("weightLbs");
        String lengthStr = request.getParameter("lengthInches");
        String method = request.getParameter("method");
        String notes = request.getParameter("notes");

        // Validation
        if (locationIdStr == null || speciesIdStr == null || catchDate == null ||
            locationIdStr.trim().isEmpty() || speciesIdStr.trim().isEmpty() || catchDate.trim().isEmpty()) {
            request.setAttribute("error", "Location, species, and catch date are required.");
            loadDropdowns(request);
            request.setAttribute("pageTitle", "Log a Catch");
            request.getRequestDispatcher("/WEB-INF/jsp/reportForm.jsp").forward(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            int locationId = Integer.parseInt(locationIdStr);
            int speciesId = Integer.parseInt(speciesIdStr);

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO CatchReports (user_id, location_id, species_id, catch_date, weight_lbs, length_inches, method, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setInt(2, locationId);
            ps.setInt(3, speciesId);
            ps.setDate(4, java.sql.Date.valueOf(catchDate));

            if (weightStr != null && !weightStr.trim().isEmpty()) {
                ps.setBigDecimal(5, new java.math.BigDecimal(weightStr.trim()));
            } else {
                ps.setNull(5, Types.DECIMAL);
            }
            if (lengthStr != null && !lengthStr.trim().isEmpty()) {
                ps.setBigDecimal(6, new java.math.BigDecimal(lengthStr.trim()));
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            if (method != null && !method.trim().isEmpty()) {
                ps.setString(7, method.trim());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setString(8, notes != null ? notes.trim() : null);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            int newId = 0;
            if (keys.next()) {
                newId = keys.getInt(1);
            }

            // Update or insert LocationSpecies
            PreparedStatement checkLs = conn.prepareStatement(
                "SELECT evidence_count FROM LocationSpecies WHERE location_id = ? AND species_id = ?");
            checkLs.setInt(1, locationId);
            checkLs.setInt(2, speciesId);
            ResultSet lsRs = checkLs.executeQuery();

            if (lsRs.next()) {
                PreparedStatement updateLs = conn.prepareStatement(
                    "UPDATE LocationSpecies SET evidence_count = evidence_count + 1 " +
                    "WHERE location_id = ? AND species_id = ?");
                updateLs.setInt(1, locationId);
                updateLs.setInt(2, speciesId);
                updateLs.executeUpdate();
            } else {
                PreparedStatement insertLs = conn.prepareStatement(
                    "INSERT INTO LocationSpecies (location_id, species_id, evidence_count) VALUES (?, ?, 1)");
                insertLs.setInt(1, locationId);
                insertLs.setInt(2, speciesId);
                insertLs.executeUpdate();
            }

            session.setAttribute("successMessage", "Catch report logged!");
            response.sendRedirect(request.getContextPath() + "/report/edit?id=" + newId);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void loadDropdowns(HttpServletRequest request) throws ServletException {
        try (Connection conn = DBUtil.getConnection()) {
            // Load locations
            List<Map<String, Object>> locations = new ArrayList<>();
            PreparedStatement ps1 = conn.prepareStatement("SELECT location_id, name FROM Locations ORDER BY name");
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("locationId", rs1.getInt("location_id"));
                m.put("name", rs1.getString("name"));
                locations.add(m);
            }
            request.setAttribute("locations", locations);

            // Load species
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
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
