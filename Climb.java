public class Climb{
    public static final int MIN_GRADE = 0;
    public static final int MAX_GRADE = 17;
    public static final int MIN_ATTEMPTS = 1;
    public static final int MAX_ATTEMPTS = 99;

    private final String name;
    private final int grade;
    private final int attempts;
    private final boolean sent;

    public Climb(String name, int grade, int attempts, boolean sent){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Name can not be empty");
        }
        if(grade < MIN_GRADE || grade > MAX_GRADE){
            throw new IllegalArgumentException("Grade must be " + MIN_GRADE + "-" + MAX_GRADE + ", got " + grade);
        }
        if(attempts < MIN_ATTEMPTS || attempts > MAX_ATTEMPTS){
            throw new IllegalArgumentException("Attempts must be "+ MIN_ATTEMPTS + "-" + MAX_ATTEMPTS + ", got " + attempts);
        }

        this.name = name.trim();
        this.grade = grade;
        this.attempts = attempts;
        this.sent = sent;
    }

    public String getName(){
        return name;
    }

    public int getGrade(){
        return grade;
    }

    public int getAttempts(){
        return attempts;
    }

    public boolean isSent(){
        return sent;
    }

    public boolean isFlash(){
        return sent && attempts == 1;
    }

    @Override
    public String toString()
    {
        String status = (sent) ? (isFlash() ? "⚡Flash ✅" : "Sent ✅") : "In Progress...";
        return "Name: " + name + ", Grade: V" + grade +
            "\nAttempts: " + attempts + " Status: " + status; 
    }
}