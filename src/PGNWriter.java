import java.io.FileWriter;
import java.io.IOException;

public class PGNWriter {

    private FileWriter fileWrtiter;


    public PGNWriter(String filename) throws IOException {
        try
        {
            fileWrtiter = new FileWriter(filename);
        }catch(IOException e)
        {
            System.err.println("Error writing to file " + filename);
        }
    }

    public void writeToPGN(String content) throws IOException {
        try
        {
            fileWrtiter.write(content);
        }catch(IOException e)
        {
            System.err.println("Error writing to file ");
        }
    }

    public void appendtoPGN(String content) throws IOException {
        try
        {
            fileWrtiter.append(content);
        }catch(IOException e)
        {
            System.err.println("Error writing to file ");
        }
    }

    public void close() throws IOException {
        fileWrtiter.close();
    }
}