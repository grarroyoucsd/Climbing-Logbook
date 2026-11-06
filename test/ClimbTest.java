// javac -cp "lib/*" -d out *.java test/*.java
// java -cp "out:lib/*" org.junit.platform.console.ConsoleLauncher execute --scan-class-path out

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClimbTest{

    @Test
    void gradeAboveMaxRejected(){
        assertThrows(IllegalArgumentException.class, 
            () -> new Climb("name", Climb.MAX_GRADE + 1, 1, true));
    }

    @Test
    void gradeBelowMinRejected(){
        assertThrows(IllegalArgumentException.class,
            () -> new Climb("name", Climb.MIN_GRADE - 1, 1, true));
    }

    @Test
    void attAboveMaxRejected(){
        assertThrows(IllegalArgumentException.class,
            () -> new Climb("name", 1, Climb.MAX_ATTEMPTS + 1, true));
    }

    @Test
    void attBelowMinRejected(){
        assertThrows(IllegalArgumentException.class,
            () -> new Climb("name", 1, Climb.MIN_ATTEMPTS - 1, true));
    }

    @Test
    void correctGradeBoundaries(){
        assertDoesNotThrow(() -> new Climb("name", Climb.MAX_GRADE, 1, true));
        assertDoesNotThrow(() -> new Climb("name", Climb.MIN_GRADE, 1, true));
    }

    @Test
    void correctAttBoundaries(){
        assertDoesNotThrow(() -> new Climb("name", 1, Climb.MAX_ATTEMPTS, true));
        assertDoesNotThrow(() -> new Climb("name", 1, Climb.MIN_ATTEMPTS, true));
    }

    @Test
    void climbIsFlash(){
        Climb c = new Climb("name", 3, 1, true);
        assertTrue(c.isFlash());
    }

    @Test 
    void climbIsNotFlash(){
        Climb c = new Climb("name", 1, 1, false);
        assertFalse(c.isFlash());
    }

    @Test
    void nullNameRejected(){
        assertThrows(IllegalArgumentException.class,
            () -> new Climb(null, 1, 1, true));
    }

    @Test
    void emptyNameRejected(){
        assertThrows(IllegalArgumentException.class,
            () -> new Climb("", 1, 1, true));
    }

    @Test 
    void blankNameRejected(){
        assertThrows(IllegalArgumentException.class,
            () -> new Climb("    ", 1, 1, true));
    }

    @Test
    void nameTrimmed(){
        Climb c = new Climb("   name   ", 1, 1, true);
        assertEquals("name", c.getName());
    }
}