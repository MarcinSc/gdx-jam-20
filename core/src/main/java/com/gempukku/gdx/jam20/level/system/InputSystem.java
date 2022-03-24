package com.gempukku.gdx.jam20.level.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.gempukku.libgdx.graph.time.TimeProvider;

public class InputSystem {
    public enum Direction {
        Up(0, 1, Input.Keys.UP, Input.Keys.W),
        Down(0, -1, Input.Keys.DOWN, Input.Keys.S),
        Right(1, 0,Input.Keys.RIGHT, Input.Keys.D),
        Left(-1, 0, Input.Keys.LEFT, Input.Keys.A);

        private int[] keys;
        private int x;
        private int y;

        Direction(int x, int y, int... keys) {
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

        int[] getKeys() {
            return keys;
        }
    }

    private boolean pauseRequested;
    private TimeProvider timeProvider;
    private LastPlayerInput lastPlayerInput = new LastPlayerInput();

    public InputSystem(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void update() {
        pauseRequested = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);

        Direction pressedDirection = getPressedDirection();
        if (pressedDirection != null) {
            lastPlayerInput.setTime(timeProvider.getTime());
            lastPlayerInput.setDirection(pressedDirection);
        }
    }

    public boolean isPauseRequested() {
        return pauseRequested;
    }

    public LastPlayerInput getLastPlayerInput() {
        return lastPlayerInput;
    }

    private Direction getPressedDirection() {
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

    private boolean isKeyPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key))
                return true;
        }
        return false;
    }
}
