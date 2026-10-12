//javac -cp "lib/*" *.java
//java --enable-native-access=ALL-UNNAMED -cp ".:lib/*" Main

import java.util.Scanner;
import java.nio.file.Path;

public class Main{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        InputReader input = new InputReader(scanner);
        ConsolePrinter printer = new ConsolePrinter();
        // ClimbRepository repo = new TextFileRepository(Path.of("history.txt"));
        ClimbRepository repo = new SqliteRepository(Path.of("climbing.db"));
        ConsoleUI console = new ConsoleUI(input, repo, printer);
        console.run();
    }
}