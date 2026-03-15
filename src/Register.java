import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;

@WebServlet("/register")
public class Register extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/login_info_db?autoReconnect=true&useSSL=false",
                    "root",
                    "YOUR_PASSWORD_HERE"
            );

            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");

            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();

            conn.close();
            response.sendRedirect("login.html");

        } catch (Exception e) {
            response.getWriter().println("Registration failed");
        }
    }
}