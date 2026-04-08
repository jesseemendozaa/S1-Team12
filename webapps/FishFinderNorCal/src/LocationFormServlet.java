import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/location/new")
public class LocationFormServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        loadLocationTypes(request);
        request.setAttribute("pageTitle", "Add Location");
        request.getRequestDispatcher("/WEB-INF/jsp/locationForm.jsp").forward(request, response);
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
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String region = request.getParameter("region");
        String latitudeStr = request.getParameter("latitude");
        String longitudeStr = request.getParameter("longitude");
        String typeIdStr = request.getParameter("typeId");

        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "Location name is required.");
            loadLocationTypes(request);
            request.setAttribute("pageTitle", "Add Location");
            request.getRequestDispatcher("/WEB-INF/jsp/locationForm.jsp").forward(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Locations (name, description, latitude, longitude, region, type_id, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name.trim());
            ps.setString(2, description != null ? description.trim() : null);

            if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                ps.setBigDecimal(3, new java.math.BigDecimal(latitudeStr.trim()));
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
                ps.setBigDecimal(4, new java.math.BigDecimal(longitudeStr.trim()));
            } else {
                ps.setNull(4, Types.DECIMAL);
            }

            ps.setString(5, region != null ? region.trim() : null);

            if (typeIdStr != null && !typeIdStr.trim().isEmpty()) {
                ps.setInt(6, Integer.parseInt(typeIdStr));
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, userId);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            int newId = 0;
            if (keys.next()) {
                newId = keys.getInt(1);
            }

            session.setAttribute("successMessage", "Location added successfully!");
            response.sendRedirect(request.getContextPath() + "/location?id=" + newId);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void loadLocationTypes(HttpServletRequest request) throws ServletException {
        List<Map<String, Object>> types = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT type_id, type_name FROM LocationTypes ORDER BY type_name");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> t = new HashMap<>();
                t.put("typeId", rs.getInt("type_id"));
                t.put("typeName", rs.getString("type_name"));
                types.add(t);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
        request.setAttribute("locationTypes", types);
    }
}
