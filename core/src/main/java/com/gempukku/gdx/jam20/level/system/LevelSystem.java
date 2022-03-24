package com.gempukku.gdx.jam20.level.system;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gempukku.gdx.jam20.level.GameLevel;
import com.gempukku.gdx.jam20.level.LevelObject;
import com.gempukku.libgdx.graph.pipeline.PipelineRenderer;
import com.gempukku.libgdx.graph.plugin.sprites.GraphSprites;
import com.gempukku.libgdx.graph.time.TimeKeeper;
import com.gempukku.libgdx.lib.camera2d.focus.CameraFocus;

import static com.gempukku.gdx.jam20.level.system.InputSystem.Direction.*;

public class LevelSystem implements CameraFocus {
    public enum FinishReason {
        Success("Congratulations!", false),
        Run_Out_Of_Time("You've run out of time", true),
        Killed_By_Enemy("Fox got to you first", true),
        Crushed_By_Rock("You've been crushed by rock", true);

        private String text;
        private boolean retry;

        FinishReason(String text, boolean retry) {
            this.text = text;
            this.retry = retry;
        }

        public boolean isRetry() {
            return retry;
        }

        public String getText() {
            return text;
        }
    }

    private GameLevel gameLevel;
    private Array<LevelObjectSprite> levelObjectSprites = new Array<>();
    private LevelObjectSprite playerSprite;

    private int collectablesCollected;
    private FinishReason finishReason;

    private TimeKeeper timeKeeper;
    private InputSystem inputSystem;
    private SoundSystem soundSystem;
    private GraphSprites graphSprites;
    private TextureAtlas textureAtlas;

    public LevelSystem(TimeKeeper timeKeeper, InputSystem inputSystem,
                       SoundSystem soundSystem, PipelineRenderer pipelineRenderer) {
        this.timeKeeper = timeKeeper;
        this.inputSystem = inputSystem;
        this.soundSystem = soundSystem;
        this.graphSprites = pipelineRenderer.getPluginData(GraphSprites.class);
    }

    public FinishReason getFinishReason() {
        return finishReason;
    }

    public int getCollectablesCollected() {
        return collectablesCollected;
    }

    public int getCollectablesRequired() {
        return gameLevel.getRequiredCollectables();
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    public void loadLevel(GameLevel gameLevel, TextureAtlas textureAtlas) {
        this.gameLevel = gameLevel;
        this.textureAtlas = textureAtlas;

        LevelObject[][] levelData = gameLevel.getLevelData();

        int y = 0;
        for (LevelObject[] rowData : levelData) {
            int x = 0;
            for (LevelObject levelObject : rowData) {
                LevelObjectSprite sprite = spawnObject(levelObject, x, gameLevel.getHeight() - y);
                if (sprite != null) {
                    if (levelObject == LevelObject.Player) {
                        playerSprite = sprite;
                    }
                    levelObjectSprites.add(sprite);
                }
                x++;
            }
            y++;
        }
    }

    public void unloadLevel() {
        for (LevelObjectSprite levelObjectSprite : levelObjectSprites) {
            levelObjectSprite.removeTag("Animated");
        }
        levelObjectSprites.clear();
        playerSprite.removeTag("Animated");

        gameLevel = null;
        collectablesCollected = 0;
        finishReason = null;
    }

    private LevelObjectSprite spawnObject(LevelObject levelObject, int x, int y) {
        if (levelObject.isSpawnSprite()) {
            LevelObjectSprite sprite = new LevelObjectSprite(x, y, levelObject, graphSprites);
            sprite.setPosition(x, y);
            TextureAtlas.AtlasRegion textureRegion = textureAtlas.findRegion(levelObject.getSpriteRegionName());
            sprite.getPropertyContainer().setValue("Texture", textureRegion);
            sprite.getPropertyContainer().setValue("Size", new Vector2(1, 1));

            sprite.addTag("Animated");

            return sprite;
        }
        return null;
    }

    private LevelObjectSprite getSpriteOfType(LevelObject levelObject) {
        for (LevelObjectSprite levelObjectSprite : levelObjectSprites) {
            if (levelObjectSprite.getType() == levelObject)
                return levelObjectSprite;
        }
        return null;
    }

    private LevelObjectSprite getSpriteAt(int x, int y) {
        for (LevelObjectSprite sprite : levelObjectSprites) {
            if (sprite.getX() == x && sprite.getY() == y)
                return sprite;
        }
        return null;
    }

    @Override
    public Vector2 getFocus(Vector2 focus) {
        return focus.set(playerSprite.getX(), playerSprite.getY());
    }

    public void update(boolean majorUpdate) {
        if (gameLevel.getMaximumLevelTime() <= MathUtils.floor(timeKeeper.getTime()))
            finishReason = FinishReason.Run_Out_Of_Time;
        else if (majorUpdate) {
            processPlayer();
            checkForPlayerDeath();
            processEnemies();
            checkForPlayerDeath();
            processStones();
        }
    }

    private void checkForPlayerDeath() {
        if (finishReason == null) {
            int playerX = playerSprite.getX();
            int playerY = playerSprite.getY();

            for (InputSystem.Direction value : values()) {
                LevelObjectSprite spriteAt = getSpriteAt(playerX + value.getX(), playerY + value.getY());
                if (spriteAt != null && spriteAt.getType().isToxicToPlayer()) {
                    finishReason = FinishReason.Killed_By_Enemy;
                }
            }
        }
    }

    private void processPlayer() {
        LastPlayerInput lastPlayerInput = inputSystem.getLastPlayerInput();
        InputSystem.Direction requestedDirection = lastPlayerInput.getDirection();
        if (requestedDirection != null) {
            processPlayerMovement(requestedDirection);
            lastPlayerInput.setDirection(null);
        }
    }

    private void processEnemies() {
        for (LevelObjectSprite sprite : new Array.ArrayIterator<>(levelObjectSprites)) {
            if (sprite.getType() == LevelObject.Fox) {
                LevelObjectSprite spriteAbove = getSpriteAt(sprite.getX(), sprite.getY() + 1);
                if (spriteAbove != null && spriteAbove.getType() == LevelObject.Stone)
                    break;

                InputSystem.Direction lastMove = sprite.getLastMove();
                InputSystem.Direction firstDirection = getPreviousDirection(lastMove);
                InputSystem.Direction checkDirection = firstDirection;

                do {
                    int nextX = sprite.getX() + checkDirection.getX();
                    int belowY = sprite.getY() + checkDirection.getY();

                    LevelObjectSprite spriteToCheck = getSpriteAt(nextX, belowY);
                    if (spriteToCheck == null) {
                        sprite.setPosition(nextX, belowY);
                        sprite.setLastMove(checkDirection);
                        break;
                    }
                    checkDirection = getNextDirection(checkDirection);
                } while (checkDirection != firstDirection);
            }
        }
    }

    private InputSystem.Direction getNextDirection(InputSystem.Direction direction) {
        switch (direction) {
            case Up:
                return Right;
            case Left:
                return Up;
            case Down:
                return Left;
            case Right:
                return Down;
        }
        return null;
    }

    private InputSystem.Direction getPreviousDirection(InputSystem.Direction direction) {
        switch (direction) {
            case Up:
                return Left;
            case Left:
                return Down;
            case Down:
                return Right;
            case Right:
                return Up;
        }
        return null;
    }

    private void processStones() {
        for (LevelObjectSprite sprite : new Array.ArrayIterator<>(levelObjectSprites)) {
            if (sprite.getType() == LevelObject.Stone) {
                int belowX = sprite.getX();
                int belowY = sprite.getY() - 1;

                LevelObjectSprite spriteBelow = getSpriteAt(belowX, belowY);
                if (spriteBelow != null && spriteBelow.getType().canBeCrushed()) {
                    // Player can only be crushed by already falling stone
                    if (sprite.isFalling() || spriteBelow.getType() != LevelObject.Player) {
                        processStoneCrushedObject(spriteBelow);
                        spriteBelow = getSpriteAt(belowX, belowY);
                    }
                }

                boolean falling = false;
                if (spriteBelow == null) {
                    sprite.setPosition(belowX, belowY);
                    falling = true;
                } else if (getSpriteAt(belowX + 1, belowY) == null && getSpriteAt(belowX + 1, belowY + 1) == null) {
                    sprite.setPosition(belowX + 1, belowY);
                    falling = true;
                } else if (getSpriteAt(belowX - 1, belowY) == null && getSpriteAt(belowX - 1, belowY + 1) == null) {
                    sprite.setPosition(belowX - 1, belowY);
                    falling = true;
                }
                sprite.setFalling(falling);
            }
        }
    }

    private void processStoneCrushedObject(LevelObjectSprite sprite) {
        LevelObject type = sprite.getType();
        switch (type) {
            case Player:
                finishReason = FinishReason.Crushed_By_Rock;
                soundSystem.playSound("death");
                break;
            case Fox:
                removeSprite(sprite);
                soundSystem.playSound("fox-death");
                break;
        }
    }

    private void processPlayerMovement(InputSystem.Direction direction) {
        int newX = playerSprite.getX() + direction.getX();
        int newY = playerSprite.getY() + direction.getY();

        LevelObjectSprite spriteAtPosition = getSpriteAt(newX, newY);
        if (spriteAtPosition == null || spriteAtPosition.getType().canBeEntered()) {
            if (spriteAtPosition != null)
                processPlayerEntering(spriteAtPosition);
            playerSprite.setPosition(newX, newY);
        } else if (spriteAtPosition.getType().canBeMoved()) {
            int otherX = newX + direction.getX();
            int otherY = newY + direction.getY();
            if (getSpriteAt(otherX, otherY) == null) {
                spriteAtPosition.setPosition(otherX, otherY);
                playerSprite.setPosition(newX, newY);
            }
        }
    }

    private void processPlayerEntering(LevelObjectSprite sprite) {
        switch (sprite.getType()) {
            case Dirt:
                removeSprite(sprite);
                soundSystem.playSound("eat-dirt");
                break;
            case Grub:
                removeSprite(sprite);
                soundSystem.playSound("eat-grub");
                collectablesCollected++;
                updateCollectablesCount();
                break;
            case Exit_Active:
                removeSprite(sprite);
                soundSystem.playSound("exit-level");
                gameFinished();
                break;
        }
    }

    private void gameFinished() {
        finishReason = FinishReason.Success;
    }

    private void removeSprite(LevelObjectSprite sprite) {
        sprite.removeTag("Animated");
        levelObjectSprites.removeValue(sprite, true);
    }

    private void updateCollectablesCount() {
        if (collectablesCollected == getCollectablesRequired()) {
            LevelObjectSprite exit = getSpriteOfType(LevelObject.Exit_Inactive);
            removeSprite(exit);
            LevelObjectSprite sprite = spawnObject(LevelObject.Exit_Active, exit.getX(), exit.getY());
            soundSystem.playSound("open-door");
            levelObjectSprites.add(sprite);
        }
    }
}
