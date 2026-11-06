import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.time.YearMonth;
import java.util.ArrayList;

public class StatsTest{

    StatsService stats;

    @BeforeEach
    void setUp(){
        Session s1 = new Session(LocalDate.of(2026, 10, 25));
        s1.addClimb(new Climb("a", 3, 1, true));
        s1.addClimb(new Climb("b", 5, 3, true));
        s1.addClimb(new Climb("c", 7, 6, false));

        Session s2 = new Session(LocalDate.of(2026, 10, 29));
        s2.addClimb(new Climb("d", 4, 1, true));
        s2.addClimb(new Climb("e", 6, 4, true));

        Session s3 = new Session(LocalDate.of(2026, 11, 02));
        s3.addClimb(new Climb("f", 6, 5, true));
        s3.addClimb(new Climb("g", 5, 1, false));

        stats = new StatsService(List.of(s1, s2, s3));
    }

    @Test 
    void totals(){
        assertEquals(3, stats.getTotalSessions());
        assertEquals(7, stats.getTotalClimbs());
        assertEquals(5, stats.getTotalSends());
        assertEquals(2, stats.getTotalFlashes());
        assertEquals(21, stats.getTotalAttempts());
    }

    @Test
    void rates(){
        assertEquals(5.0/7, stats.getSendRate(), 0.0001);
        assertEquals(2.0/7, stats.getFlashRate(), 0.0001);
        assertEquals(21.0/7, stats.getAvgAttemptsClimb(), 0.0001);
    }

    @Test
    void totalsByGrade(){
        assertArrayEquals(new int[]{0,0,0,1,1,1,2,0,0,0,0,0,0,0,0,0,0,0}, stats.getSendsByGrade());
        assertArrayEquals(new int[]{0,0,0,1,1,3,9,0,0,0,0,0,0,0,0,0,0,0}, stats.getAttForSendsByGrade());
        assertArrayEquals(new int[]{0,0,0,1,1,2,2,1,0,0,0,0,0,0,0,0,0,0}, stats.getClimbsByGrade());
    }

    @Test
    void sendRateByGrade(){
        SendRatio[] rates = stats.getSendRateByGrade();
        assertEquals(Climb.MAX_GRADE + 1, rates.length);

        for (int i = 0; i < 3; ++i){
            assertEquals(0.0, rates[i].rate());
        } 
        assertEquals(1.0, rates[3].rate());
        assertEquals(1.0, rates[4].rate());
        assertEquals(0.5, rates[5].rate());
        assertEquals(1.0, rates[6].rate());
        assertEquals(0.0, rates[7].rate());
        for (int i = 8; i < Climb.MAX_GRADE + 1; ++i){
            assertEquals(0.0, rates[i].rate());
        }
    }

    @Test
    void avgGradeSent(){
        assertEquals(4.8, stats.getAverageGradeSent());
    }

    @Test
    void hardestSend(){
        DatedClimb dc = stats.getHardestClimb();
        assertEquals(LocalDate.of(2026, 10, 29), dc.date());

        Climb c = dc.climb();
        assertEquals("e", c.getName());
    }

    @Test
    void hardestFlash(){
        DatedClimb dc = stats.getHardestFlash();
        assertEquals(LocalDate.of(2026, 10, 29), dc.date());

        Climb c = dc.climb();
        assertEquals("d", c.getName());
    }

    @Test
    void longestProj(){
        DatedClimb dc = stats.getLongestProject();
        assertEquals(LocalDate.of(2026, 11, 02), dc.date());
        
        Climb c = dc.climb();
        assertEquals("f", c.getName());
    }

    @Test
    void bestSesh(){
        Session sesh = stats.getBestSession();
        assertEquals(LocalDate.of(2026, 10, 29), sesh.getDate());
    }

    @Test
    void sessionsPerMonth(){
        Map<YearMonth, Integer> map = stats.getSessionsPerMonth();

        assertEquals(2, map.get(YearMonth.of(2026, 10)));
        assertEquals(1, map.get(YearMonth.of(2026, 11)));
    }

    @Test
    void monthlyHardestSends(){
        Map<YearMonth, List<GradeSends>> map = stats.getHardestSendsByMonth(2);

        List<GradeSends> oct = map.get(YearMonth.of(2026, 10));
        List<GradeSends> nov = map.get(YearMonth.of(2026, 11));

        assertEquals(2, oct.size());
        assertEquals(6, oct.get(0).grade());
        assertEquals(1, oct.get(0).sends());
        assertEquals(4, oct.get(0).attempts());
        assertEquals(5, oct.get(1).grade());
        assertEquals(1, oct.get(1).sends());
        assertEquals(3, oct.get(1).attempts());

        assertEquals(1, nov.size());
        assertEquals(6, nov.get(0).grade());
        assertEquals(1, nov.get(0).sends());
        assertEquals(5, nov.get(0).attempts());
    }

    @Test
    void monthlySendRateByGrade(){
        Map<YearMonth, SendRatio[]> map = stats.getSendRateByGradeByMonth();

        SendRatio[] oct = map.get(YearMonth.of(2026, 10));

        for(int i = 0; i < 3; ++i){
            assertEquals(0.0, oct[i].rate());
        }
        assertEquals(1.0, oct[3].rate());
        assertEquals(1.0, oct[4].rate());
        assertEquals(1.0, oct[5].rate());
        assertEquals(1.0, oct[6].rate());
        assertEquals(0.0, oct[7].rate());
        for(int i = 8; i < Climb.MAX_GRADE + 1; ++i){
            assertEquals(0.0, oct[i].rate());
        }

        SendRatio[] nov = map.get(YearMonth.of(2026, 11));

        for(int i = 0; i < 5; ++i){
            assertEquals(0.0, nov[i].rate());
        }
        assertEquals(0.0, nov[5].rate());
        assertEquals(1.0, nov[6].rate());
        for(int i = 7; i < Climb.MAX_GRADE + 1; ++i){
            assertEquals(0.0, nov[i].rate());
        }
    }

    @Test
    void nullConstructor(){
        assertThrows(IllegalArgumentException.class,
            () -> new StatsService(null));
    }

    @Test
    void emptyList(){
        stats = new StatsService(new ArrayList<Session>());

        assertEquals(0, stats.getTotalClimbs());
        assertEquals(0, stats.getTotalSends());
        assertEquals(0, stats.getTotalFlashes());
        assertEquals(0, stats.getTotalAttempts());
        
        assertEquals(0.0, stats.getSendRate());
        assertEquals(0.0, stats.getFlashRate());
        assertEquals(0.0, stats.getAvgAttemptsClimb());

        assertEquals(0.0, stats.getAverageGradeSent());

        assertNull(stats.getHardestClimb());
        assertNull(stats.getHardestFlash());
        assertNull(stats.getLongestProject());
        assertNull(stats.getBestSession());

        Map<YearMonth, Integer> sessions = stats.getSessionsPerMonth();
        Map<YearMonth, List<GradeSends>> hardest = stats.getHardestSendsByMonth(2);
        Map<YearMonth, SendRatio[]> rates = stats.getSendRateByGradeByMonth();

        assertTrue(sessions.isEmpty());
        assertTrue(hardest.isEmpty());
        assertTrue(rates.isEmpty());
    }

    @Test
    void noSends(){
        Session sesh = new Session(LocalDate.of(2026, 10, 31));
        sesh.addClimb(new Climb("1", 4, 5, false));
        sesh.addClimb(new Climb("2", 6, 3, false));
        sesh.addClimb(new Climb("3", 10, 9, false));
        stats = new StatsService(List.of(sesh));

        assertEquals(0, stats.getTotalSends());
        assertEquals(0, stats.getTotalFlashes());

        assertEquals(0.0, stats.getSendRate());
        assertEquals(0.0, stats.getFlashRate());

        assertEquals(0.0, stats.getAverageGradeSent());

        assertNull(stats.getHardestClimb());
        assertNull(stats.getHardestFlash());
        assertNull(stats.getLongestProject());
        assertNull(stats.getBestSession());

        Map<YearMonth, Integer> sessions = stats.getSessionsPerMonth();
        Map<YearMonth, List<GradeSends>> hardest = stats.getHardestSendsByMonth(2);

        assertEquals(1, sessions.size());
        assertEquals(1, hardest.size());
        assertTrue(hardest.get(YearMonth.of(2026, 10)).isEmpty());
    }

    @Test
    void invalidHardestSendsByMonthParam(){
        assertThrows(IllegalArgumentException.class,
            () -> stats.getHardestSendsByMonth(0));
    }
}