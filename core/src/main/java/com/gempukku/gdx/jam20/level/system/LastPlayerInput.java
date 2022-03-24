package com.gempukku.gdx.jam20.level.system;

public class LastPlayerInput {
    private float time;
    private InputSystem.Direction direction;

    public void setTime(float time) {
        this.time = time;
    }

    public void setDirection(InputSystem.Direction direction) {
        this.direction = direction;
    }

    public float getTime() {
        return time;
    }

    public InputSystem.Direction getDirection() {
        return direction;
    }
}
