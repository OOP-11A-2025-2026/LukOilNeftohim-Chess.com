package io;

import java.io.*;

import model.Game;

public class Storage {

    public static Game readGame(String fileName) {
        String text = readPGN(fileName);

        Parser parser = new Parser();
        Game game = parser.parseGame(text);

        return game;
    }

    public static void writeGame(String fileName, Game game) {
        StringBuilder sb = new StringBuilder();

        // tags
        game.getTags().forEach((k, v) -> {
            sb.append("[").append(k).append(" \"")
              .append(v).append("\"]\n");
        });

        sb.append("\n");

        int moveNo = 1;
        int lineLen = 0;
        for (int i = 0; i < game.getMoves().size(); i++) {
            if (i % 2 == 0) {
                String prefix = moveNo + ". ";
                sb.append(prefix);
                lineLen += prefix.length();
                moveNo++;
            }

            String move = game.getMoves().get(i) + " ";
            sb.append(move);
            lineLen += move.length();

            if (lineLen >= 80) {
                sb.append("\n");
                lineLen = 0;
            }
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
            throw new RuntimeException("Cannot read PGN: " + fileName, e);
        }
        return sb.toString();
    }

    private static void writePGN(String fileName, String content) {
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write PGN: " + fileName, e);
        }
    }
}
