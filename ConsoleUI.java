import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

import java.util.ArrayList;

public class ConsoleUI{
    private final InputReader input;
    private final ClimbRepository repo;

    public ConsoleUI(InputReader input, ClimbRepository repo){
        this.input = input;
        this.repo = repo;
    }

    public Session readSession(){
        LocalDate date = input.readDate("Date of Session yyyy-MM-dd (or press Enter for today): ");

        Session sesh = new Session(date);

        while(true){
            String name = input.readString("Enter climb name (or 'done' to finish): ");

            if(name.equalsIgnoreCase("done")) break;

            int grade = input.readInt("Enter V grade (" + Climb.MIN_GRADE + "-" + Climb.MAX_GRADE + "): ", 
                Climb.MIN_GRADE, Climb.MAX_GRADE);
            int attempts = input.readInt("Enter attempts (" + Climb.MIN_ATTEMPTS + "-" + Climb.MAX_ATTEMPTS + "): ",
                Climb.MIN_ATTEMPTS, Climb.MAX_ATTEMPTS);
            boolean sent = input.readYesNo("Was climb sent? ");

            Climb climb = new Climb(name, grade, attempts, sent);

            sesh.addClimb(climb);
        }

        return sesh;
    }

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

    public void printStats(StatsService stats){
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
            System.out.println("Hardest Grade Sent: -");
        }else{
            System.out.printf("Avg Grade Sent: V%.1f%n", stats.getAverageGradeSent());
            System.out.println("Hardest Grade Sent: V" + stats.getHardestGrade());
        }

        System.out.println("\nGrade Breakdown");

        int[] climbs = stats.getClimbsByGrade();
        int[] sends = stats.getSendsByGrade();
        double[] sendRates = stats.getSendRateByGrade();

        for (int i = Climb.MIN_GRADE; i <= Climb.MAX_GRADE; ++i){
            if(climbs[i] > 0){
                System.out.printf("V%-3d Attempted: %3d Sent: %3d %5.1f%%%n", i, climbs[i], sends[i], sendRates[i] * 100);
            }
        }
    }

    public void run(){
        while(true){
            System.out.println("1. Log Session");
            System.out.println("2. View History");
            System.out.println("3. View Stats");
            System.out.println("4. Quit");
            System.out.println("");

            int num = input.readInt("Enter number (1-4) to choose an option: ", 1, 4);
            System.out.println("");

            try{
                if(num == 1){
                    handleLogSession();
                } else if(num == 2){
                    handleHistory();
                } else if(num == 3){
                    handleStats();
                } else if(num == 4){
                    break;
                }
            } catch(StorageException e){
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println("");
        }
    }

    public void handleLogSession(){
        Session sesh = readSession();
        printSession(sesh);

        if(sesh.getTotalClimbs() != 0){
            boolean confirm = input.readYesNo("Do you want to save session (yes/no)? ");
            if(confirm){
                repo.saveSession(sesh);
                System.out.println("Session saved");
            }
        }
    }

    public void handleHistory(){
        List<Session> seshList = repo.loadAllSessions();

        List<Session> display = new ArrayList<>(seshList);
        Collections.reverse(display);

        if(display.size() == 0){
            System.out.println("No climbing data yet");
        }else{
            printHistory(display);
            System.out.println("");

            boolean more = input.readYesNo("Do you want to view a session in detail (yes/no)? ");
            while(more){
                int seshNum = input.readInt("Enter number of session: ", 1, display.size());
                printSession(display.get(seshNum - 1));

                more = input.readYesNo("Do you want to view another session (yes/no)? ");
            }
        }
    }

    public void handleStats(){
        List<Session> seshList = repo.loadAllSessions();
        StatsService stats = new StatsService(seshList);

        printStats(stats);
    }
}