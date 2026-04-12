import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/species/edit")
public class SpeciesEditServlet extends HttpServlet {
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
                "SELECT species_id, common_name, scientific_name FROM Species WHERE species_id = ?");
            ps.setInt(1, Integer.parseInt(idStr));
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
            request.setAttribute("pageTitle", "Edit Species");
            request.getRequestDispatcher("/WEB-INF/jsp/speciesForm.jsp").forward(request, response);
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

        String idStr = request.getParameter("speciesId");
        String commonName = request.getParameter("commonName");
        String scientificName = request.getParameter("scientificName");

        if (idStr == null || commonName == null || commonName.trim().isEmpty()) {
            request.setAttribute("error", "Common name is required.");
            Map<String, Object> species = new HashMap<>();
            species.put("speciesId", idStr);
            species.put("commonName", commonName);
            species.put("scientificName", scientificName);
            request.setAttribute("species", species);
            request.setAttribute("pageTitle", "Edit Species");
            request.getRequestDispatcher("/WEB-INF/jsp/speciesForm.jsp").forward(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Species SET common_name = ?, scientific_name = ? WHERE species_id = ?");
            ps.setString(1, commonName.trim());
            ps.setString(2, scientificName != null && !scientificName.trim().isEmpty() ? scientificName.trim() : null);
            ps.setInt(3, Integer.parseInt(idStr));
            ps.executeUpdate();

            session.setAttribute("successMessage", "Species updated successfully.");
            response.sendRedirect(request.getContextPath() + "/species?id=" + idStr);
        } catch (SQLIntegrityConstraintViolationException e) {
            request.setAttribute("error", "Species name already exists.");
            Map<String, Object> species = new HashMap<>();
            species.put("speciesId", idStr);
            species.put("commonName", commonName);
            species.put("scientificName", scientificName);
            request.setAttribute("species", species);
            request.setAttribute("pageTitle", "Edit Species");
            request.getRequestDispatcher("/WEB-INF/jsp/speciesForm.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
