import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class InputReader{
    private final Scanner scanner;

    public InputReader(Scanner scanner){
        this.scanner = scanner;
    }

    public String readString(String prompt){
        while(true){
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            if(!line.isBlank()){
                return line;
            }
            System.out.println("Input can't be empty");
        }
    }

    public int readInt(String prompt, int min, int max){
        while(true){
            System.out.print(prompt);
            try{
                int num = Integer.parseInt(scanner.nextLine().trim());

                if(num >= min && num <= max){
                    return num;
                }
                System.out.println("Input is not in " + min + "-" + max);
            } catch(NumberFormatException e){
                System.out.println("Input must be a number");
            }
        }
    }

    public GradeRange readRange(String prompt, int min, int max, int maxSpan){
        System.out.println(prompt);
        int resMin = readInt("Enter lower bound: ", min, max);
        int resMax = readInt("Enter Upper bound: ", resMin, Math.min(max, resMin + maxSpan));
        return new GradeRange(resMin, resMax);
    }

    public boolean readYesNo(String prompt){
        while(true){
            System.out.print(prompt);
            String line = scanner.nextLine().trim().toLowerCase();

            if(line.equals("yes") || line.equals("y")){
                return true;
            } else if(line.equals("no") || line.equals("n")){
                return false;
            }
            System.out.println("Input must be yes/no or y/n");
        }
    }

    public LocalDate readDate(String prompt){
        while(true){
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            if(line.isEmpty()){
                return LocalDate.now();
            }

            try{
                LocalDate date = LocalDate.parse(line);
                return date;
            }catch(DateTimeParseException e){
                System.out.println("Not a valid date, use yyyy-MM-dd");
            }
        }
    }
}