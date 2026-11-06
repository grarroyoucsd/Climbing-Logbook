import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class SessionTest{

    private Session s;

    @BeforeEach
    void setUp(){
        s = new Session(LocalDate.now());
    }

    @Test
    void nullDate(){
        assertThrows(IllegalArgumentException.class,
            () -> new Session(null));
    }

    @Test
    void totalClimbs(){
        assertEquals(0, s.getTotalClimbs());

        s.addClimb(new Climb("name", 1, 1, true));
        assertEquals(1, s.getTotalClimbs());
    }

    @Test
    void checkCounts(){
        s.addClimb(new Climb("1", 1, 1, true));
        s.addClimb(new Climb("2", 1, 2, true));
        s.addClimb(new Climb("3", 1, 1, false));

        assertEquals(2, s.getSentCount());
        assertEquals(1, s.getFlashCount());
    }

    @Test
    void totalAttempts(){
        assertEquals(0, s.getTotalAttempts());

        s.addClimb(new Climb("1", 5, 4, true));
        assertEquals(4, s.getTotalAttempts());

        s.addClimb(new Climb("2", 6, 7, false));
        assertEquals(11, s.getTotalAttempts());
    }

    @Test
    void nullClimb(){
        assertThrows(IllegalArgumentException.class,
            () -> s.addClimb(null));
    }

    @Test
    void unmodifiableClimbList(){
        assertThrows(UnsupportedOperationException.class, 
            () -> s.getClimbs().add(new Climb("name", 1, 1, true))); 
    }

    @Test
    void hardestGrade(){
        s.addClimb(new Climb("name", 10, 3, false));

        assertEquals(-1, s.getHardestGrade());

        s.addClimb(new Climb("1", 5, 2, true));
        s.addClimb(new Climb("2", 8, 3, true));
        s.addClimb(new Climb("3", 7, 5, true));

        assertEquals(8, s.getHardestGrade());
    }
}