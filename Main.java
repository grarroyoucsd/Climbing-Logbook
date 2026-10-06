import java.util.Scanner;
import java.nio.file.Path;

public class Main{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        InputReader input = new InputReader(scanner);
        TextFileRepository fileRep = new TextFileRepository(Path.of("history.txt"));
        ConsoleUI console = new ConsoleUI(input, fileRep);
        console.run();
    }
}