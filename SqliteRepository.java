import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SqliteRepository implements ClimbRepository{
    private final String url;

    public SqliteRepository(Path dbFile){
        if (dbFile == null){
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.url = "jdbc:sqlite:" + dbFile;

        try(Connection conn = connect()){
            createTables(conn);
        }catch(SQLException e){
            throw new StorageException("Could not set up database at " + dbFile, e);
        }
    }

    private Connection connect() throws SQLException{
        Connection conn = DriverManager.getConnection(url);
        try(Statement st = conn.createStatement()){
            st.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    private void createTables(Connection conn) throws SQLException{
        try(Statement st = conn.createStatement()){
            st.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS climbs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id INTEGER NOT NULL REFERENCES sessions(id),
                    name TEXT NOT NULL,
                    grade INTEGER NOT NULL CHECK (grade BETWEEN %d AND %d),
                    attempts INTEGER NOT NULL CHECK (attempts BETWEEN %d AND %d),
                    sent INTEGER NOT NULL CHECK (sent IN (0, 1))
                )""".formatted(Climb.MIN_GRADE, Climb.MAX_GRADE, Climb.MIN_ATTEMPTS, Climb.MAX_ATTEMPTS));
        }
    }

    @Override
    public void saveSession(Session sesh){
        if(sesh == null){
            throw new IllegalArgumentException("Session cannot be NULL");
        }

        if(sesh.getTotalClimbs() == 0){
            return;
        }

        try(Connection conn = connect()){
            conn.setAutoCommit(false);
            try{
                int sessionId;

                String sessionSql = "INSERT INTO sessions (date) VALUES (?)";
                try(PreparedStatement ps = conn.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS)){
                    ps.setString(1, sesh.getDate().toString());
                    ps.executeUpdate();

                    try(ResultSet keys = ps.getGeneratedKeys()){
                        keys.next();
                        sessionId = keys.getInt(1);
                    }
                }

                String climbSql = "INSERT INTO climbs (session_id, name, grade, attempts, sent) VALUES (?, ?, ?, ?, ?)";
                try(PreparedStatement ps = conn.prepareStatement(climbSql)){
                    for (Climb c : sesh.getClimbs()){
                        ps.setInt(1, sessionId);
                        ps.setString(2, c.getName());
                        ps.setInt(3, c.getGrade());
                        ps.setInt(4, c.getAttempts());
                        int sent = (c.isSent()) ? 1 : 0;
                        ps.setInt(5, sent);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
            }catch(SQLException e){
                conn.rollback();
                throw e;
            }
        }catch(SQLException e){
            throw new StorageException("Could not save session to database", e);
        }
    }

    @Override
    public List<Session> loadAllSessions(){
        LinkedHashMap<Integer, Session> map = new LinkedHashMap<>();

        String sql = "SELECT s.id, s.date, c.name, c.grade, c.attempts, c.sent "
                + "FROM sessions s JOIN climbs c ON c.session_id = s.id "
                + "ORDER BY s.date, s.id, c.id";
        try(Connection conn = connect(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)){

            while(rs.next()){
                int sessionId = rs.getInt("id");

                Session sesh = map.get(sessionId);
                if(sesh == null){
                    LocalDate date = LocalDate.parse(rs.getString("date"));
                    sesh = new Session(date);
                    map.put(sessionId, sesh);
                }

                String name = rs.getString("name");
                int grade = rs.getInt("grade");
                int attempts = rs.getInt("attempts");
                boolean sent = rs.getInt("sent") == 1;

                sesh.addClimb(new Climb(name, grade, attempts, sent));
            }
        }catch(SQLException | DateTimeParseException | IllegalArgumentException e){
            throw new StorageException("Could not load session from database", e);
        }

        return new ArrayList<Session>(map.values());
    }

    @Override
    public void deleteAllData(){
        try(Connection conn = connect()){
            conn.setAutoCommit(false);
            try{
                try(Statement st = conn.createStatement()){
                    st.execute("DROP TABLE IF EXISTS climbs");
                    st.execute("DROP TABLE IF EXISTS sessions");
                }
                conn.commit();
                createTables(conn);
            }catch(SQLException e){
                conn.rollback();
                throw e;
            }
        }catch(SQLException e){
            throw new StorageException("Could not delete data", e);
        }
    }
}