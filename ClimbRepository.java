import java.util.List;

public interface ClimbRepository{

    void saveSession(Session sesh);

    List<Session> loadAllSessions();

}