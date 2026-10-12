import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteTest{
    public static void main(String[] args) throws SQLException{
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            Statement st = conn.createStatement()){
                
            st.execute("CREATE TABLE IF NOT EXISTS hello (id INTEGER PRIMARY KEY, message TEXT)");
            st.execute("INSERT INTO hello (message) VALUES ('it works')");

            try (ResultSet rs = st.executeQuery("SELECT id, message FROM hello")){
                while(rs.next()){
                    System.out.println(rs.getInt("id") + ": " + rs.getString("message"));
                }
            }
        }
    }
}