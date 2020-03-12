package uj.java2019.w11.pong;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class BattleshipGame extends BattleShipHelper {
    final String miss = "pudlo";
    final String hit = "trafiony";
    final String dead = "trafiony zatopiony";
    final String gameLost = "ostatni zatopiony";

    enum CellStatus {SHIP, WATER, UNKNOWN, SHOT, MISS}

    private CellStatus[][] myGrid;
    private CellStatus[][] enemyGrid;

    int lastShotHeight = 0; // my -> enemy
    int lastShotWidth = 0;
    int lastEnemyShotHeight = 0; // enemy -> my
    int lastEnemyShotWidth = 0;

    BattleshipGame(String fileName) {
//        myGrid = init(CellStatus.WATER);
        enemyGrid = init(CellStatus.UNKNOWN);
        myGrid = readMapFromFile(fileName);
    }

    private CellStatus[][] init(CellStatus status) {
        CellStatus[][] grid = new CellStatus[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                grid[i][j] = status;
            }
        }
        return grid;
    }

    void handleEnemyResponse(String resp) {
        String enemyResponse = parseInputLine(resp);
        if(enemyResponse.equals(gameLost)){
            System.out.println("Wygrana");
            applyEnemyResponse(enemyResponse);
            print(myGrid);
            System.out.println();
            print(enemyGrid);
            return;
        }
        String enemyWantToShotAt = parseWhereEnemyWantsToShot(resp);
        applyEnemyResponse(enemyResponse);
        enemyShotMe(enemyWantToShotAt);
    }

    private void enemyShotMe(String indexes) {
        lastEnemyShotHeight = getShotHeight(indexes);
        lastEnemyShotWidth = getShotWidth(indexes);

        switch (myGrid[lastEnemyShotHeight][lastEnemyShotWidth]) {
            case WATER:
                myGrid[lastEnemyShotHeight][lastEnemyShotWidth] = CellStatus.MISS;
                break;
            case SHIP:
                myGrid[lastEnemyShotHeight][lastEnemyShotWidth] = CellStatus.SHOT;
                break;
        }
        System.out.println("MY MAP");
        print(myGrid);
    }

    String fullResponseForEnemy() {
        if (stillAlive()) return String.format("%s;%s\n", responseForEnemy(), randomWhereToShotAtEnemy());
        System.out.println("Przegrana");
        // TODO print enemy

        print(enemyGrid);
        System.out.println();
        print(myGrid);
        return gameLost + '\n';
    }

    private String responseForEnemy() {
        switch (myGrid[lastEnemyShotHeight][lastEnemyShotWidth]) {
            case MISS:
                return miss;
            case SHOT:
                return shipIsDead()? dead : hit;
        }
        return "";
    }

    private boolean shipIsDead(){
        HashSet<String> setA = new HashSet<String>();
        shipIndexes(lastEnemyShotHeight, lastEnemyShotWidth, setA, myGrid);
        for (String indexes : setA) {
            int h = getShotHeight(indexes);
            int w = getShotWidth(indexes);
            if (myGrid[h][w] == CellStatus.SHIP) return false;
        }
        return true;
    }


    private void shipIndexes(int h, int w, HashSet<String> buff, CellStatus[][] grid){
        if(h < 0 || h >= HEIGHT || w < 0 || w >= HEIGHT) return;
        switch (grid[h][w]){
            case WATER:
            case MISS:
            case UNKNOWN:
                return;
            case SHIP:
            case SHOT:
                String indexes = indexesToString(h, w);
                if (buff.contains(indexes)) return;
                buff.add(indexes);
        }
        shipIndexes(h + 1, w, buff, grid);
        shipIndexes(h - 1, w, buff, grid);
        shipIndexes(h, w + 1, buff, grid);
        shipIndexes(h, w - 1, buff, grid);
    }

    private boolean stillAlive() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (myGrid[i][j] == CellStatus.SHIP) return true;
            }
        }
        return false;
    }

    public String randomWhereToShotAtEnemy() {
        lastShotHeight = ThreadLocalRandom.current().nextInt(0, HEIGHT);
        lastShotWidth = ThreadLocalRandom.current().nextInt(0, WIDTH);
        return indexesToString(lastShotHeight, lastShotWidth);
    }

    void applyEnemyResponse(String prevResult) {
        // todo ostatni zatopiony
        switch (prevResult) {
            case miss:
                enemyGrid[lastShotHeight][lastShotWidth] = CellStatus.MISS;
                break;
            case hit:
                enemyGrid[lastShotHeight][lastShotWidth] = CellStatus.SHOT;
                break;
            case gameLost:
            case dead:
                enemyGrid[lastShotHeight][lastShotWidth] = CellStatus.SHOT;
                killedEnemyShip();
            default:
                break;
        }
        System.out.println("ENEMY MAP");
        print(enemyGrid);
    }

    void killedEnemyShip(){
        HashSet<String> enemyShipIndexes = new HashSet<String>();
        shipIndexes(lastShotHeight, lastShotWidth, enemyShipIndexes, enemyGrid);

        for (String indexes : enemyShipIndexes) {
            int h = getShotHeight(indexes);
            int w = getShotWidth(indexes);
            setPointToMiss(h + 1, w + 1);
            setPointToMiss(h + 1, w - 1);
            setPointToMiss(h - 1, w + 1);
            setPointToMiss(h - 1, w - 1);
            setPointToMiss(h, w - 1);
            setPointToMiss(h, w + 1);
            setPointToMiss(h + 1, w);
            setPointToMiss(h - 1, w);
        }
    }

    void setPointToMiss(int h, int w){
        try {
            if (enemyGrid[h][w] == CellStatus.UNKNOWN) enemyGrid[h][w] = CellStatus.MISS;
        } catch (IndexOutOfBoundsException e){
            // no trudno
        }
    }

    /*

    . . . .
    . # . .
    . # # .
    . . # .
    . . . .


    */
}
