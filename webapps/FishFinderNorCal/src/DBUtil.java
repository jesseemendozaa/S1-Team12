import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String HOST = getEnvOrDefault("DB_HOST", "localhost");
    private static final String PORT = getEnvOrDefault("DB_PORT", "3307");
    private static final String NAME = getEnvOrDefault("DB_NAME", "fishfindernorcaldb");
    private static final String USER = getEnvOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");
    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + NAME
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found", e);
        }
        if (PASSWORD == null || PASSWORD.trim().isEmpty()) {
            throw new SQLException(
                "DB_PASSWORD is not set. Configure DB_HOST, DB_PORT, DB_NAME, DB_USER, and DB_PASSWORD before starting Tomcat."
            );
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
