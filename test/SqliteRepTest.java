import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

import java.util.List;
import java.time.LocalDate;

public class SqliteRepTest{

    @TempDir
    Path tempDir;

    SqliteRepository rep;

    @BeforeEach
    void setuUp(){
        rep = new SqliteRepository(tempDir.resolve("test.db"));
    }

    @Test
    void emptyLoading(){
        assertTrue(rep.loadAllSessions().isEmpty());
    }

    @Test
    void roundTrip(){
        Session sesh = new Session(LocalDate.of(2026, 11, 05));
        sesh.addClimb(new Climb("a", 3, 1, true));
        sesh.addClimb(new Climb("b", 5, 3, true));
        sesh.addClimb(new Climb("c", 7, 7, false));
        rep.saveSession(sesh);

        List<Session> seshList = rep.loadAllSessions();
        assertEquals(1, seshList.size());

        Session loaded = seshList.get(0);

        assertEquals(sesh.getDate(), loaded.getDate());
        assertEquals(sesh.getTotalClimbs(), loaded.getTotalClimbs());

        for(int i = 0; i < 3; ++i){
            Climb expected = sesh.getClimbs().get(i);
            Climb actual = loaded.getClimbs().get(i);

            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getGrade(), actual.getGrade());
            assertEquals(expected.getAttempts(), actual.getAttempts());
            assertEquals(expected.isSent(), actual.isSent());
        }
    }

    @Test
    void saveEmpty(){
        rep.saveSession(new Session(LocalDate.of(2026, 11, 05)));
        assertTrue(rep.loadAllSessions().isEmpty());
    }

    @Test
    void saveNull(){
        assertThrows(IllegalArgumentException.class,
            () -> rep.saveSession(null));
    }

    @Test
    void testOrder(){
        Session sesh1 = new Session(LocalDate.of(2026, 11, 05));
        sesh1.addClimb(new Climb("a", 3, 1, true));
        sesh1.addClimb(new Climb("b", 5, 3, true));
        sesh1.addClimb(new Climb("c", 7, 7, false));

        Session sesh2 = new Session(LocalDate.of(2026, 11, 06));
        sesh2.addClimb(new Climb("d", 5, 3, true));

        rep.saveSession(sesh2);
        rep.saveSession(sesh1);

        List<Session> seshList = rep.loadAllSessions();

        assertEquals(sesh1.getDate(), seshList.get(0).getDate());
        assertEquals(sesh2.getDate(), seshList.get(1).getDate());
    }

    @Test
    void sameDaySessions(){
        Session sesh1 = new Session(LocalDate.of(2026, 11, 05));
        sesh1.addClimb(new Climb("a", 3, 1, true));
        sesh1.addClimb(new Climb("b", 5, 3, true));
        sesh1.addClimb(new Climb("c", 7, 7, false));

        Session sesh2 = new Session(LocalDate.of(2026, 11, 05));
        sesh2.addClimb(new Climb("d", 5, 3, true));

        rep.saveSession(sesh1);
        rep.saveSession(sesh2);

        assertEquals(2, rep.loadAllSessions().size());
    }

    @Test
    void delete(){
        Session sesh1 = new Session(LocalDate.of(2026, 11, 05));
        sesh1.addClimb(new Climb("a", 3, 1, true));
        sesh1.addClimb(new Climb("b", 5, 3, true));
        sesh1.addClimb(new Climb("c", 7, 7, false));
        rep.saveSession(sesh1);

        rep.deleteAllData();
        assertTrue(rep.loadAllSessions().isEmpty());

        Session sesh2 = new Session(LocalDate.of(2026, 11, 06));
        sesh2.addClimb(new Climb("d", 5, 3, true));
        rep.saveSession(sesh2);

        List<Session> list = rep.loadAllSessions();

        assertEquals(1, list.size());
        assertEquals(sesh2.getDate(), list.get(0).getDate());
        assertEquals(sesh2.getClimbs().get(0).getName(), list.get(0).getClimbs().get(0).getName());
    }

    @Test
    void persistenceAcrossInstances(){
        Session sesh = new Session(LocalDate.of(2026, 11, 05));
        sesh.addClimb(new Climb("a", 3, 1, true));
        rep.saveSession(sesh);

        SqliteRepository rep2 = new SqliteRepository(tempDir.resolve("test.db"));
        assertEquals(1, rep2.loadAllSessions().size());
    }
}