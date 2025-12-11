import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PGNreader {
    private BufferedReader reader;

    PGNreader(String fileName) {
        try {
            this.reader = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            System.out.println("File not found or cannot be opened.");
            this.reader = null;
        }
    }

    public String read() {
        if (this.reader == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }

        return text.toString();
    }
}