import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseHandler {
    private static final String JDBC_URL = "jdbc:mysql://your_database_url";
    private static final String USER = "root";
    private static final String PASSWORD = "Kumar@123";

    public static void addFavoriteSong(String songName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            String query = "INSERT INTO favorites (song_name) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, songName);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Other database operations can be added here
}
