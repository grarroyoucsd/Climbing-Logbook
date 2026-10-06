import java.util.List;

interface ClimbRepository{

    void saveSession(Session sesh);

    List<Session> loadAllSessions();

}