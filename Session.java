import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Session{
    private final LocalDate date;
    private final List<Climb> climbList;

    public Session(LocalDate date){
        if (date == null){
            throw new IllegalArgumentException("Date cannot be NULL");
        }
        this.date = date;
        climbList = new ArrayList<>();
    }

    public void addClimb(Climb climb){
        if(climb == null){
            throw new IllegalArgumentException("Climb cannot be NULL");
        }
        climbList.add(climb);
    }

    public int getTotalClimbs(){
        return climbList.size();
    }

    public LocalDate getDate(){
        return date;
    }

    public List<Climb> getClimbs(){
        return Collections.unmodifiableList(climbList);
    }

    public int getSentCount(){
        int count = 0;

        for (Climb c : climbList){
            if(c.isSent()){
                ++count;
            }
        }

        return count;
    }

    public int getFlashCount(){
        int count = 0;

        for (Climb c : climbList){
            if(c.isFlash()){
                ++count;
            }
        }

        return count;
    }

    public int getTotalAttempts(){
        int count = 0; 

        for (Climb c : climbList){
            count += c.getAttempts();
        }

        return count;
    }

    public int getHardestGrade(){
        int grade = -1;

        for(Climb c : climbList){
            if(c.isSent() && c.getGrade() > grade){
                grade = c.getGrade();
            }
        }

        return grade;
    }
}