import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;


public class ConsoleUI{
    private final InputReader input;
    private final ClimbRepository repo;
    private final ConsolePrinter printer;

    public ConsoleUI(InputReader input, ClimbRepository repo, ConsolePrinter printer){
        this.input = input;
        this.repo = repo;
        this.printer = printer;
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

    public void run(){
        while(true){
            System.out.println("1. Log Session");
            System.out.println("2. View History");
            System.out.println("3. View Stats");
            System.out.println("4. Delete Data");
            System.out.println("5. Quit");
            System.out.println();

            int num = input.readInt("Enter number (1-5) to choose an option: ", 1, 5);
            System.out.println();

            try{
                if(num == 1){
                    handleLogSession();
                } else if(num == 2){
                    handleHistory();
                } else if(num == 3){
                    handleStats();
                } else if(num == 4){
                    handleDeletion();
                }else if(num == 5){
                    break;
                }
            } catch(StorageException e){
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println();
        }
    }

    public void handleLogSession(){
        Session sesh = readSession();
        printer.printSession(sesh);

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

        if(seshList.size() == 0){
            System.out.println("No climbing data yet");
        }else{
            List<Session> display = new ArrayList<>(seshList);
            Collections.reverse(display);

            printer.printHistory(display);
            System.out.println();

            boolean more = input.readYesNo("Do you want to view a session in detail (yes/no)? ");
            while(more){
                int seshNum = input.readInt("Enter number of session: ", 1, display.size());
                printer.printSession(display.get(seshNum - 1));

                more = input.readYesNo("Do you want to view another session (yes/no)? ");
            }
        }
    }

    public void handleStats(){
        List<Session> seshList = repo.loadAllSessions();
        StatsService stats = new StatsService(seshList);

        if(seshList.size() == 0){
            System.out.println("No climbing data yet");
            return;
        }

        System.out.println("1. Overview");
        System.out.println("2. Monthly Progress");
        System.out.println("3. Highlights");
        System.out.println();

        int num = input.readInt("Enter number (1-3) to choose an option: ", 1, 3);
        System.out.println("");

        if(num == 1){
            printer.printOverview(stats);
        }else if(num == 2){
            handleMonthly(stats);
        }else if(num == 3){
            printer.printHighlights(stats);
        }
    }

    public void handleMonthly(StatsService stats){
        printer.printMonthlyOverview(stats);
        System.out.println();

        if(input.readYesNo("Would you like to look at monthly send rates (yes/no)? ")){
            System.out.println();
            GradeRange range = input.readRange("What range of grades would you like to look over (MAX of 5 grades)? ", 
                Climb.MIN_GRADE, Climb.MAX_GRADE, + 5);
            
            printer.printMonthlyRates(stats, range);
        }
    }

    public void handleDeletion(){
        if (input.readYesNo("Are you sure you want to delete your data (yes/no)? ")){
            String delete = input.readString("Enter 'DELETE' to delete data: ");
            if (delete.equals("DELETE")){
                repo.deleteAllData();
                System.out.println("Data deleted successfully");
                return;
            }
        }

        System.out.println("Data was not deleted");
    }
}