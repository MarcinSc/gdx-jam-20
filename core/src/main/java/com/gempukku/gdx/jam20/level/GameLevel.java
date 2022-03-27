package com.gempukku.gdx.jam20.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

public class GameLevel {
    private final String name;
    private final int width;
    private final int height;
    private final int requiredCollectables;
    private final int maximumLevelTime;
    private final LevelObject[][] levelData;

    public GameLevel(String name, int requiredCollectables, int maximumLevelTime, LevelObject[][] levelData) {
        this.name = name;
        this.requiredCollectables = requiredCollectables;
        this.maximumLevelTime = maximumLevelTime;
        this.levelData = levelData;
        this.width = levelData[0].length;
        this.height = levelData.length;
    }

    public static LevelObject[][] createUnvalidatedLevelData(String text) {
        Array<Array<LevelObject>> result = new Array<>();
        for (String line : text.split("\n")) {
            if (line.length() != 0) {
                char[] chars = line.toCharArray();
                Array<LevelObject> row = new Array<>();
                for (char ch : chars) {
                    row.add(getLevelObject(ch));
                }
                result.add(row);
            }
        }
        if (result.size == 0)
            throw new IllegalArgumentException("No level data");

        int width = result.get(0).size;
        for (Array<LevelObject> levelObjects : result) {
            if (levelObjects.size != width)
                throw new IllegalArgumentException("Level data has to be in a rectangle shape");
        }

        int playerCount = 0;
        int exitCount = 0;
        for (Array<LevelObject> levelObjects : result) {
            for (LevelObject levelObject : levelObjects) {
                if (levelObject == LevelObject.Player)
                    playerCount++;
                if (levelObject == LevelObject.Exit_Inactive)
                    exitCount++;
            }
        }

        if (playerCount != 1)
            throw new IllegalArgumentException("Level must have exactly one player");
        if (exitCount <= 0)
            throw new IllegalArgumentException("Level must have at least one exit");

        return createLevelLayout(result);
    }

    private static LevelObject[][] createLevelLayout(Array<Array<LevelObject>> result) {
        int width = result.get(0).size;

        LevelObject[][] levelLayout = new LevelObject[result.size + 2][];

        LevelObject[] wallRow = new LevelObject[width + 2];
        Arrays.fill(wallRow, LevelObject.Wall);

        levelLayout[0] = wallRow;
        for (int i = 0; i < result.size; i++) {
            levelLayout[i + 1] = new LevelObject[width + 2];
            levelLayout[i + 1][0] = LevelObject.Wall;
            LevelObject[] fromData = result.get(i).toArray(LevelObject.class);
            System.arraycopy(fromData, 0, levelLayout[i + 1], 1, fromData.length);
            levelLayout[i + 1][width + 1] = LevelObject.Wall;
        }
        levelLayout[result.size + 1] = wallRow;

        return levelLayout;
    }

    public String getName() {
        return name;
    }

    public int getRequiredCollectables() {
        return requiredCollectables;
    }

    public int getMaximumLevelTime() {
        return maximumLevelTime;
    }

    public LevelObject[][] getLevelData() {
        return levelData;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Vector2 getSpawnPosition() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (levelData[y][x] == LevelObject.Player)
                    return new Vector2(x, y);
            }
        }
        return null;
    }

    public static GameLevel loadGameLevel(FileHandle fileHandle) throws IOException {
        try (BufferedReader reader = fileHandle.reader(1024)) {
            String name = reader.readLine();
            int collectableCount = Integer.parseInt(reader.readLine());
            int maximumTime = Integer.parseInt(reader.readLine());
            Array<Array<LevelObject>> result = new Array<>();
            String line;
            while ((line = reader.readLine()) != null) {
                char[] chars = line.toCharArray();
                Array<LevelObject> row = new Array<>();
                for (char ch : chars) {
                    row.add(getLevelObject(ch));
                }
                result.add(row);
            }

            LevelObject[][] levelLayout = createLevelLayout(result);

            return new GameLevel(name, collectableCount, maximumTime, levelLayout);
        }
    }

    private static LevelObject getLevelObject(char c) {
        switch (c) {
            case ' ':
            case 'A':
                return LevelObject.Air;
            case 'D':
                return LevelObject.Dirt;
            case 'W':
                return LevelObject.Wall;
            case 'P':
                return LevelObject.Player;
            case 'X':
                return LevelObject.Exit_Inactive;
            case 'G':
                return LevelObject.Grub;
            case 'O':
                return LevelObject.Stone;
            case 'F':
                return LevelObject.Fox;
        }
        throw new IllegalArgumentException("Unknown type of object: " + c);
    }
}
