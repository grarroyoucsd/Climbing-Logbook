import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ConsolePrinter{
    private static final int HARDEST_GRADES = 2;

    public ConsolePrinter(){}

    public void printSession(Session sesh){
        if(sesh.getTotalClimbs() == 0){
            System.out.println("No climbs were entered");
            return;
        }

        System.out.println(sesh.getDate());

        List<Climb> climbs = sesh.getClimbs();

        int num = 0;
        for (Climb c : climbs){
            System.out.println("\n" + (++num) + ".");
            System.out.println(c);
        }

        System.out.println();
        System.out.println("Climbs: " + sesh.getTotalClimbs());
        System.out.println("Sent: " + sesh.getSentCount());
        System.out.println("Flashes: " + sesh.getFlashCount());
        System.out.println();
    }

    public void printHistory(List<Session> seshList){

        for (int i = 0; i < seshList.size(); ++i){
            Session sesh = seshList.get(i);
            int hardestGrade = sesh.getHardestGrade();
            String hardest = (hardestGrade == -1) ? "-" : "V" + hardestGrade; 
            System.out.printf("%d %s %2d climbs %2d sent %2d flashes  hardest %s%n", 
                i + 1, sesh.getDate(), sesh.getTotalClimbs(), sesh.getSentCount(), sesh.getFlashCount(), hardest);
        }
    }

    public void printOverview(StatsService stats){
        if(stats.getTotalClimbs() == 0){
            System.out.println("No climbing data yet");
            return;
        }

        System.out.println("Sessions: " + stats.getTotalSessions());
        System.out.println("Climbs: " + stats.getTotalClimbs());
        System.out.println("Sends: " + stats.getTotalSends());
        System.out.println("Flashes: " + stats.getTotalFlashes());
        System.out.printf("Send Rate: %.1f%%%n", stats.getSendRate() * 100);
        System.out.printf("Flash Rate: %.1f%%%n", stats.getFlashRate() * 100);
        System.out.printf("Avg Att per Climb: %.1f%n", stats.getAvgAttemptsClimb());
        if(stats.getTotalSends() == 0){
            System.out.println("Avg Grade Sent: -");
        }else{
            System.out.printf("Avg Grade Sent: V%.1f%n", stats.getAverageGradeSent());
        }

        System.out.println("\nGrade Breakdown");

        SendRatio[] sendRates = stats.getSendRateByGrade();

        for (int i = Climb.MIN_GRADE; i <= Climb.MAX_GRADE; ++i){
            if(sendRates[i].climbs() > 0){
                System.out.printf("V%-3d %s%n", i, formatRatio(sendRates[i]));
            }
        }
    }

    public void printMonthlyOverview(StatsService stats){
        Map<YearMonth, Integer> numSessions = stats.getSessionsPerMonth();
        Map<YearMonth, List<GradeSends>> hardestSends = stats.getHardestSendsByMonth(HARDEST_GRADES);

        for (YearMonth month : numSessions.keySet()){
            System.out.printf("%s (%d sessions)%n", month, numSessions.get(month));

            List<GradeSends> sends = hardestSends.get(month);
            if(sends.size() == 0){
                System.out.println("No sends this month");
            }else{
                System.out.print("Hardest Sends: ");
                for(int i = 0; i < sends.size(); ++i){
                    System.out.printf("%-18s",formatSends(sends.get(i)) + "\t");
                }
            }
            System.out.println();
        }
    }

    public void printMonthlyRates(StatsService stats, GradeRange range){
        Map<YearMonth, SendRatio[]> sendRates = stats.getSendRateByGradeByMonth();
        System.out.printf("%-9s", "");
        for (int i = range.min(); i <= range.max(); ++i){
            System.out.printf("%15s", "V" + i);
        }
        System.out.println();

        for (YearMonth month : sendRates.keySet()){
            System.out.printf("%-9s", month);
            SendRatio[] ratios = sendRates.get(month);
            for (int i = range.min(); i <= range.max(); ++i){
                System.out.printf("%15s", formatRatio(ratios[i]));
            }
            System.out.println();
        }
    }

    public void printHighlights(StatsService stats){
        System.out.println("Hardest Send: " + formatClimb(stats.getHardestClimb()));
        System.out.println("Hardest Flash: " + formatClimb(stats.getHardestFlash()));
        System.out.println("Longest Project: " + formatClimb(stats.getLongestProject()));
        
        System.out.println("Best Session: ");
        Session bestSesh = stats.getBestSession();

        if(bestSesh == null){
            System.out.println("No sends yet");
        }else {
            printSession(bestSesh);
        }
    }

    private static String formatClimb(DatedClimb c){
        if(c == null){
            return "-";
        }
        String result = "%s %s, V%d".formatted(c.date(), c.climb().getName(), c.climb().getGrade());
        return (c.climb().isFlash()) ? result : result + " (%d attempts)".formatted(c.climb().getAttempts());
    }

    private static String formatSends(GradeSends s){
        return "V%d x%d (%d att)".formatted(s.grade(), s.sends(), s.attempts());
    }

    private static String formatRatio(SendRatio r){
        return (r.climbs() == 0) ? "-" : "%d/%d (%.1f%%)".formatted(r.sends(), r.climbs(), r.rate() * 100);
    }
}