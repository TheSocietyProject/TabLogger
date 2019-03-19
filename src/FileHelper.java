import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class FileHelper {


    public static String getDate(){
        OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC );
        return now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth();
    }

    public static String getFilepath(){
        return Paths.get("").toAbsolutePath().toString();
    }


    public static void addStrings(String filepath, List<String> lines){
        try {
            new File(filepath).getParentFile().mkdirs();
            FileWriter fw = new FileWriter(filepath, true);
            BufferedWriter bw = new BufferedWriter(fw);

            for (String line : lines) {
                bw.newLine();
                bw.write(line);
                bw.flush();
            }

            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void addString(String filepath, String line){
        try {
            new File(filepath).getParentFile().mkdirs();
            FileWriter fw = new FileWriter(filepath, true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.newLine();
            bw.write(line);
            bw.flush();

            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
