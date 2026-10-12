import java.util.List;
import java.util.ArrayList;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;

public class StatsService{
    public static final int BASE_FOR_SESH_PTS = 2;

    private final List<Session> seshList;

    public StatsService(List<Session> seshList){
        if(seshList == null){
            throw new IllegalArgumentException("List of Sessions cannot be NULL");
        }
        this.seshList = new ArrayList<>(seshList);
    }

    public int getTotalSessions(){
        return seshList.size();
    }

    public int getTotalClimbs(){
        int climbs = 0;

        for(Session s : seshList){
            climbs += s.getTotalClimbs();
        }

        return climbs;
    }

    public int getTotalSends(){
        int sends = 0;

        for(Session s : seshList){
            sends += s.getSentCount();
        }

        return sends;
    }

    public int getTotalFlashes(){
        int flashes = 0;

        for (Session s : seshList){
            flashes += s.getFlashCount();
        }

        return flashes;
    }

    public int getTotalAttempts(){
        int attempts = 0;

        for (Session s : seshList){
            attempts += s.getTotalAttempts();
        }

        return attempts;
    }

    public double getSendRate(){
        return (getTotalClimbs() == 0) ? 0.0 : (double) getTotalSends() / getTotalClimbs();
    }

    public double getFlashRate(){
        return (getTotalClimbs() == 0) ? 0.0 : (double) getTotalFlashes() / getTotalClimbs();
    }

    public double getAvgAttemptsClimb(){
        return (getTotalClimbs() == 0) ? 0.0 : (double) getTotalAttempts() / getTotalClimbs(); 
    }

    public int[] getSendsByGrade(){
        int[] sends = new int[Climb.MAX_GRADE + 1];

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                if(c.isSent()){
                    ++sends[c.getGrade()];
                }
            }
        }

        return sends;
    }

    public int[] getAttForSendsByGrade(){
        int[] atts = new int[Climb.MAX_GRADE + 1];

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                if(c.isSent()){
                    atts[c.getGrade()] += c.getAttempts();
                }
            }
        }

        return atts;
    }

    public int[] getClimbsByGrade(){
        int[] climbs = new int[Climb.MAX_GRADE + 1];

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                ++climbs[c.getGrade()];
            }
        }

        return climbs;
    }

    public SendRatio[] getSendRateByGrade(){
        int[] sends = getSendsByGrade();
        int[] climbs = getClimbsByGrade();
        SendRatio[] sendRates = new SendRatio[Climb.MAX_GRADE + 1];

        for (int i = 0; i < sendRates.length; ++i){
            sendRates[i] = new SendRatio(sends[i], climbs[i]);
        }

        return sendRates;
    }

    public double getAverageGradeSent(){
        if(getTotalSends() == 0){
            return 0.0;
        }

        int count = 0;

        int[] sends = getSendsByGrade();
        for(int i = 0; i < sends.length; ++i){
            count += (sends[i] * i);
        }

        return (double) count / getTotalSends();
    }

    public DatedClimb getHardestClimb(){
        Climb hardest = null;
        LocalDate date = null;

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                if(c.isSent()){
                    if(hardest == null || c.getGrade() > hardest.getGrade()){
                        hardest = c;
                        date = s.getDate();
                    }
                }
            }
        }

        return (hardest == null) ? null : new DatedClimb(date, hardest);
    }

    public DatedClimb getHardestFlash(){
        Climb hardest = null;
        LocalDate date = null;

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                if(c.isFlash()){
                    if(hardest == null || c.getGrade() > hardest.getGrade()){
                        hardest = c;
                        date = s.getDate();
                    }
                }
            }
        }

        return (hardest == null) ? null : new DatedClimb(date, hardest);
    }

    public DatedClimb getLongestProject(){
        Climb longest = null;
        LocalDate date = null;

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                if(c.isSent()){
                    if(longest == null || c.getAttempts() > longest.getAttempts()){
                        longest = c;
                        date = s.getDate();
                    }
                }
            }
        }

        return (longest == null) ? null : new DatedClimb(date, longest);
    }

    public int getSessionPts(Session sesh){
        int pts = 0;
        for (Climb c : sesh.getClimbs()){
            if (c.isSent()){
                pts += Math.pow(BASE_FOR_SESH_PTS, c.getGrade());
            }
        }

        return pts;
    }

    public Session getBestSession(){
        if(seshList.size() == 0){
            return null;
        }

        Session sesh = seshList.get(0);
        int pts = getSessionPts(sesh);
        for (Session s : seshList){
            int npts = getSessionPts(s);
            if(npts >= pts){
                if(npts > pts){
                    sesh = s;
                    pts = npts;
                }else if(s.getHardestGrade() > sesh.getHardestGrade()){
                    sesh = s;
                    pts = npts;
                }
            }
        }

        return (pts == 0) ? null : sesh;
    }

    private Map<YearMonth, List<Session>> groupByMonth(){
        Map<YearMonth, List<Session>> map = new TreeMap<>();

        for (Session sesh : seshList){
            YearMonth month = YearMonth.from(sesh.getDate());
            List<Session> monthlyList = map.get(month);
            if(monthlyList == null){
                monthlyList = new ArrayList<>();
                map.put(month, monthlyList);
            }

            monthlyList.add(sesh);
        }

        return map;
    }

    public Map<YearMonth, Integer> getSessionsPerMonth(){
        Map<YearMonth, List<Session>> sessions = groupByMonth();

        Map<YearMonth, Integer> numSessions = new TreeMap<>();

        for (YearMonth month : sessions.keySet()){
            numSessions.put(month, sessions.get(month).size());
        }

        return numSessions;
    }

    public Map<YearMonth, List<GradeSends>> getHardestSendsByMonth(int n){
        if(n < 1){
            throw new IllegalArgumentException("n must be at least 1, got " + n);
        }

        Map<YearMonth, List<Session>> sessions = groupByMonth();

        Map<YearMonth, List<GradeSends>> hardest = new TreeMap<>();

        for (YearMonth month : sessions.keySet()){
            StatsService monthStats = new StatsService(sessions.get(month));

            int[] sends = monthStats.getSendsByGrade();
            int[] atts = monthStats.getAttForSendsByGrade();
            List<GradeSends> hardestSends =  new ArrayList<>();
            
            for (int i = Climb.MAX_GRADE; i >= Climb.MIN_GRADE; --i){
                if(sends[i] > 0){
                    hardestSends.add(new GradeSends(i, sends[i], atts[i]));

                    if(hardestSends.size() == n){
                        break;
                    }
                }
            }

            hardest.put(month, hardestSends);
        }

        return hardest;
    }

    public Map<YearMonth, SendRatio[]> getSendRateByGradeByMonth(){
        Map<YearMonth, List<Session>> sessions = groupByMonth();

        Map<YearMonth, SendRatio[]> sendRates = new TreeMap<>();

        for(YearMonth month : sessions.keySet()){
            StatsService monthStats = new StatsService(sessions.get(month));

            SendRatio[] sendR = monthStats.getSendRateByGrade();

            sendRates.put(month, sendR);
        }

        return sendRates;
    }
}