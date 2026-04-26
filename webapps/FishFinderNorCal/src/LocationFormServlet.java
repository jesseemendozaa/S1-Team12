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
        String city = request.getParameter("city");
        String address = request.getParameter("address");
        String typeIdStr = request.getParameter("typeId");

        if (name == null || name.trim().isEmpty() ||
            city == null || city.trim().isEmpty() ||
            address == null || address.trim().isEmpty() ||
            typeIdStr == null || typeIdStr.trim().isEmpty()) {
            request.setAttribute("error", "Location name, city, address, and type are required.");
            request.setAttribute("location", buildLocationFormState(name, description, city, address, typeIdStr));
            loadLocationTypes(request);
            request.setAttribute("pageTitle", "Add Location");
            request.getRequestDispatcher("/WEB-INF/jsp/locationForm.jsp").forward(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement duplicatePs = conn.prepareStatement(
                "SELECT location_id FROM Locations " +
                "WHERE location_name = ? AND city = ? AND address = ?");
            duplicatePs.setString(1, name);
            duplicatePs.setString(2, city);
            duplicatePs.setString(3, address);
            ResultSet duplicateRs = duplicatePs.executeQuery();
            if (duplicateRs.next()) {
                request.setAttribute("error", "That fishing location already exists.");
                request.setAttribute("location", buildLocationFormState(name, description, city, address, typeIdStr));
                loadLocationTypes(request);
                request.setAttribute("pageTitle", "Add Location");
                request.getRequestDispatcher("/WEB-INF/jsp/locationForm.jsp").forward(request, response);
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Locations (location_name, description, city, address, type_id, created_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name.trim());
            ps.setString(2, description != null ? description.trim() : null);
            ps.setString(3, city != null ? city.trim() : null);
            ps.setString(4, address != null ? address.trim() : null);

            if (typeIdStr != null && !typeIdStr.trim().isEmpty()) {
                ps.setInt(5, Integer.parseInt(typeIdStr));
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(6, userId);
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

    static Map<String, Object> buildLocationFormState(
            String name, String description, String city, String address, String typeIdStr) {
        Map<String, Object> location = new HashMap<>();
        location.put("name", name != null ? name.trim() : null);
        location.put("description", description != null ? description.trim() : null);
        location.put("city", city != null ? city.trim() : null);
        location.put("address", address != null ? address.trim() : null);
        location.put("typeId", typeIdStr != null && !typeIdStr.trim().isEmpty() ? typeIdStr.trim() : null);
        return location;
    }

    static void loadLocationTypes(HttpServletRequest request) throws ServletException {
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
