import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.TreeMap;
import java.util.Map;
import java.time.format.DateTimeParseException;

public class TextFileRepository implements ClimbRepository{
    private final Path file;

    public TextFileRepository(Path file){
        if(file == null){
            throw new IllegalArgumentException("Path must not be NULL");
        }

        this.file = file;
    }

    @Override
    public void saveSession(Session sesh){
        if(sesh == null){
            throw new IllegalArgumentException("Session cannot be NULL");
        }

        if(sesh.getTotalClimbs() == 0){
            return;
        }

        List<String> lines = new ArrayList<>();

        for(Climb c : sesh.getClimbs()){
            String line = sesh.getDate() + "\t" + c.getName().replace('\t', ' ') + "\t" + c.getGrade() + 
                "\t" + c.getAttempts() + "\t" + c.isSent();
            lines.add(line);
        }

        try{
            Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }catch(IOException e){
            throw new StorageException("Could not save Session to " + file, e);
        }
    }

    @Override 
    public List<Session> loadAllSessions(){
        if(!Files.exists(file)){
            return new ArrayList<Session>();
        }

        List<String> lines;
        try{
            lines = Files.readAllLines(file);
        }catch(IOException e){
            throw new StorageException("Could not read from " + file, e);
        }

        Map<LocalDate, Session> map = new TreeMap<>();

        for (int i = 0; i < lines.size(); ++i){
            String line = lines.get(i);

            if(line.isBlank()){
                continue;
            }

            String[] args = line.split("\t");

            int lineNum = i + 1;

            if(args.length != 5){
                throw new StorageException("Wrong number of arguments at line " + lineNum + ": " + line);
            }

            try{
                LocalDate date = LocalDate.parse(args[0]);

                Session sesh = map.get(date);
                if(sesh == null){
                    sesh = new Session(date);
                    map.put(date, sesh);
                }

                String name = args[1];
                int grade = Integer.parseInt(args[2]);
                int attempts = Integer.parseInt(args[3]);
                if(!args[4].equals("true") && !args[4].equals("false")){
                    throw new StorageException("Invalid Data at line " + lineNum + ": " + line);
                }
                boolean sent = Boolean.parseBoolean(args[4]);

                sesh.addClimb(new Climb(name, grade, attempts, sent));

            }catch(DateTimeParseException | IllegalArgumentException e){
                throw new StorageException("Invalid Data at line " + lineNum + ": " + line, e);
            }
        }

        return new ArrayList<Session>(map.values());
    }
}