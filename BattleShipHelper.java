package uj.java2019.w11.pong;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BattleShipHelper {
    public static final int HEIGHT = 10;
    public static final int WIDTH = 10;
    final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    final char ship = '#';
    final char water = '.';
    final char miss = '~';
    final char shot = '@';
    final char unknown = '?';

    protected void print(BattleshipGame.CellStatus[][] m) {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                switch (m[i][j]) {
                    case SHIP:
                        System.out.print(ship);
                        break;
                    case WATER:
                        System.out.print(water);
                        break;
                    case MISS:
                        System.out.print(miss);
                        break;
                    case SHOT:
                        System.out.print(shot);
                        break;
                    case UNKNOWN:
                        System.out.print(unknown);
                }
            }
            System.out.println();
        }
    }

    public int getShotHeight(String indexes) {
        return Integer.parseInt((indexes.substring(1).trim().strip())) - 1;
    }

    public int getShotWidth(String indexes) {
        return Integer.parseInt(String.valueOf(alphabet.indexOf(String.valueOf(indexes.charAt(0)))));
    }

    public String indexesToString(int height, int width) {
        return String.valueOf(alphabet.charAt(width)) + (height + 1);
    }

    public BattleshipGame.CellStatus[][] readMapFromFile(String path) {
        BattleshipGame.CellStatus[][] grid = new BattleshipGame.CellStatus[HEIGHT][WIDTH];
        ArrayList<String> lines = readFile(path);
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                switch (lines.get(i).charAt(j)) {
                    case water:
                        grid[i][j] = BattleshipGame.CellStatus.WATER;
                        break;
                    case ship:
                        grid[i][j] = BattleshipGame.CellStatus.SHIP;
                        break;
                    default:
                        System.out.println("Bad format in file " + path + " line " + (i + 1) + " at index " + j + 1);
                }
            }
        }
        return grid;
    }

    private ArrayList<String> readFile(String file) {
        ArrayList<String> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                result.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String parseInputLine(String inputLine) {
        return inputLine.strip().split(";")[0].strip();
    }

    public String parseWhereEnemyWantsToShot(String inputLine) {
        return inputLine.strip().split(";")[1].strip();
    }
}
