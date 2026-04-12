import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/location/edit")
public class LocationEditServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"moderator".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT location_id, location_name, description, city, address, type_id " +
                "FROM Locations WHERE location_id = ?");
            ps.setInt(1, Integer.parseInt(idStr));
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendRedirect(request.getContextPath() + "/locations");
                return;
            }

            Map<String, Object> location = new HashMap<>();
            location.put("locationId", rs.getInt("location_id"));
            location.put("name", rs.getString("location_name"));
            location.put("description", rs.getString("description"));
            location.put("city", rs.getString("city"));
            location.put("address", rs.getString("address"));
            location.put("typeId", rs.getObject("type_id"));
            request.setAttribute("location", location);
            request.setAttribute("editMode", true);
            LocationFormServlet.loadLocationTypes(request);
            request.setAttribute("pageTitle", "Edit Location");
            request.getRequestDispatcher("/WEB-INF/jsp/locationForm.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"moderator".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String locationIdStr = request.getParameter("locationId");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String city = request.getParameter("city");
        String address = request.getParameter("address");
        String typeIdStr = request.getParameter("typeId");

        if (locationIdStr == null || name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "Location name is required.");
            request.setAttribute("editMode", true);
            Map<String, Object> location = LocationFormServlet.buildLocationFormState(
                name, description, city, address, typeIdStr);
            location.put("locationId", locationIdStr);
            request.setAttribute("location", location);
            LocationFormServlet.loadLocationTypes(request);
            request.setAttribute("pageTitle", "Edit Location");
            request.getRequestDispatcher("/WEB-INF/jsp/locationForm.jsp").forward(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Locations SET location_name = ?, description = ?, city = ?, address = ?, type_id = ? " +
                "WHERE location_id = ?");
            ps.setString(1, name.trim());
            ps.setString(2, description != null ? description.trim() : null);
            ps.setString(3, city != null ? city.trim() : null);
            ps.setString(4, address != null ? address.trim() : null);
            if (typeIdStr != null && !typeIdStr.trim().isEmpty()) {
                ps.setInt(5, Integer.parseInt(typeIdStr));
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, Integer.parseInt(locationIdStr));
            ps.executeUpdate();

            session.setAttribute("successMessage", "Location updated successfully.");
            response.sendRedirect(request.getContextPath() + "/location?id=" + locationIdStr);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
