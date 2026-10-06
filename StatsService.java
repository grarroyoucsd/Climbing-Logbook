import java.util.List;
import java.util.ArrayList;

public class StatsService{
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

    public int[] getClimbsByGrade(){
        int[] climbs = new int[Climb.MAX_GRADE + 1];

        for (Session s : seshList){
            for (Climb c : s.getClimbs()){
                ++climbs[c.getGrade()];
            }
        }

        return climbs;
    }

    public double[] getSendRateByGrade(){
        int[] sends = getSendsByGrade();
        int[] climbs = getClimbsByGrade();
        double[] sendRates = new double[Climb.MAX_GRADE + 1];

        for (int i = 0; i < sendRates.length; ++i){
            sendRates[i] = (climbs[i] == 0) ? 0.0 : (double) sends[i] / climbs[i];
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

    public int getHardestGrade(){
        int[] sends = getSendsByGrade();

        for (int i = Climb.MAX_GRADE; i >= 0; --i){
            if (sends[i] > 0){
                return i;
            }
        }

        return -1;
    }
}