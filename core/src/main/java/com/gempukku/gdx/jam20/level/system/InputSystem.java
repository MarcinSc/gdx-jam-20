package com.gempukku.gdx.jam20.level.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputSystem {
    public enum Direction {
        Up("up", 0, 1, Input.Keys.UP, Input.Keys.W),
        Down("down", 0, -1, Input.Keys.DOWN, Input.Keys.S),
        Right("right", 1, 0, Input.Keys.RIGHT, Input.Keys.D),
        Left("left", -1, 0, Input.Keys.LEFT, Input.Keys.A);

        private int[] keys;
        private String name;
        private int x;
        private int y;

        Direction(String name, int x, int y, int... keys) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.keys = keys;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int[] getKeys() {
            return keys;
        }

        public String getName() {
            return name;
        }
    }

    private boolean pauseRequested;

    private Direction memorizedDirection;
    private Direction pressedDirection;
    private boolean pressedSinceReset;

    public void update() {
        pauseRequested = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);

        Direction lastPressedDirection = pressedDirection;
        pressedDirection = getPressedDirection();
        if (lastPressedDirection == null && pressedDirection != null)
            pressedSinceReset = true;

        if (lastPressedDirection != null && pressedDirection == null)
            memorizedDirection = lastPressedDirection;
    }

    public boolean isPauseRequested() {
        return pauseRequested;
    }

    public void resetPlayerInput() {
        pressedSinceReset = false;
        memorizedDirection = null;
    }

    public Direction getPlayerInput() {
        if (pressedDirection != null)
            return pressedDirection;
        if (pressedSinceReset)
            return memorizedDirection;
        return null;
    }

    private static Direction getPressedDirection() {
        Direction pressedDirection = null;
        for (Direction value : Direction.values()) {
            if (isKeyPressed(value.getKeys())) {
                if (pressedDirection != null) {
                    // More than 1 diretion key pressed
                    return null;
                } else {
                    pressedDirection = value;
                }
            }
        }
        return pressedDirection;
    }

    private static boolean isKeyPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key))
                return true;
        }
        return false;
    }
}
