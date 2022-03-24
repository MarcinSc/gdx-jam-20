package com.gempukku.gdx.jam20.level.system;

import com.badlogic.gdx.math.Vector3;
import com.gempukku.gdx.jam20.level.LevelObject;
import com.gempukku.libgdx.graph.plugin.sprites.GraphSprites;
import com.gempukku.libgdx.graph.util.sprite.CommonPropertiesSpriteAdapter;

public class LevelObjectSprite extends CommonPropertiesSpriteAdapter {
    private InputSystem.Direction lastMove = InputSystem.Direction.Up;
    private boolean falling;
    private int x;
    private int y;
    private LevelObject type;

    public LevelObjectSprite(int x, int y, LevelObject type, GraphSprites graphSprites) {
        super(graphSprites);

        this.x = x;
        this.y = y;
        this.type = type;
    }

    public InputSystem.Direction getLastMove() {
        return lastMove;
    }

    public void setLastMove(InputSystem.Direction lastMove) {
        this.lastMove = lastMove;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;

        Vector3 position = getPosition();
        position.set(x, y, position.z);
        updateSprite();
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public LevelObject getType() {
        return type;
    }

    public void setType(LevelObject type) {
        this.type = type;
    }
}

