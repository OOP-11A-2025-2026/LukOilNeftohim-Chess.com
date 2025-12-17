import java.io.*;
import java.util.*;

public class Storage {

    public static Game readGame(String fileName) {
        Game game = new Game();
        String text = readPGN(fileName);

        int idx = 0;
        while (idx < text.length() && text.charAt(idx) == '[') {
            int end = text.indexOf(']', idx);
            if (end == -1) break;
            String tagLine = text.substring(idx+1, end);
            int space = tagLine.indexOf(' ');
            if (space > 0) {
                String key = tagLine.substring(0, space);
                String value = tagLine.substring(space+1).replaceAll("\"", "");
                game.getTags().put(key, value);
            }
            idx = end + 1;
        }

        String movesText = text.substring(idx).replaceAll("\\d+\\.", "")
                                .replaceAll("\\{[^}]*\\}", "")
                                .trim();
        String[] tokens = movesText.split("\\s+");
        for (String token : tokens) {
            if (token.isEmpty() || token.equals("1-0") || token.equals("0-1") || token.equals("1/2-1/2")) 
                continue;
            // TODO 
            // Move move = Parser.parseSAN(token, game.getBoard());
            // game.getMoves().add(move);
            // game.getBoard().move(move.getFrom(), move.getTo());
        }

        return game;
    }

    public static void writeGame(String fileName, Game game) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : game.getTags().entrySet()) {
            sb.append("[").append(entry.getKey()).append(" \"")
              .append(entry.getValue()).append("\"]\n");
        }
        sb.append("\n");

        int count = 1;
        List<Move> moves = game.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) sb.append(count++).append(". ");
            // sb.append(moves.get(i).toSAN()).append(" ");
        }

        writePGN(fileName, sb.toString().trim());
    }

    private static String readPGN(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
        }
        return sb.toString();
    }

    private static void writePGN(String fileName, String content) {
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(content);
        } catch (IOException e) {
            System.err.println("Error writing file: " + fileName);
        }
    }
}
