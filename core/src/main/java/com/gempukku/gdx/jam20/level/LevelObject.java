package com.gempukku.gdx.jam20.level;

public enum LevelObject {
    Air(null, true),

    Exit_Inactive("exit-inactive"),
    Wall("wall"),

    Dirt("dirt", true),
    Exit_Active("exit-active", true),
    Grub("grub", true),

    Stone("stone", false, true),

    Player("player-right", false, false, true),
    Fox("fox", false, false, true, true);

    private final boolean canBeEntered;
    private final boolean canBeMoved;
    private final String spriteRegionName;
    private final boolean canBeCrushed;
    private final boolean toxicToPlayer;

    LevelObject(String spriteRegionName) {
        this(spriteRegionName, false);
    }

    LevelObject(String spriteRegionName, boolean canBeEntered) {
        this(spriteRegionName, canBeEntered, false);
    }

    LevelObject(String spriteRegionName, boolean canBeEntered, boolean canBeMoved) {
        this(spriteRegionName, canBeEntered, canBeMoved, false);
    }

    LevelObject(String spriteRegionName, boolean canBeEntered, boolean canBeMoved, boolean canBeCrushed) {
        this(spriteRegionName, canBeEntered, canBeMoved, canBeCrushed, false);
    }

    LevelObject(String spriteRegionName, boolean canBeEntered, boolean canBeMoved, boolean canBeCrushed, boolean toxicToPlayer) {
        this.spriteRegionName = spriteRegionName;
        this.canBeEntered = canBeEntered;
        this.canBeMoved = canBeMoved;
        this.canBeCrushed = canBeCrushed;
        this.toxicToPlayer = toxicToPlayer;
    }

    public boolean isSpawnSprite() {
        return spriteRegionName != null;
    }

    public String getSpriteRegionName() {
        return spriteRegionName;
    }

    public boolean canBeEntered() {
        return canBeEntered;
    }

    public boolean canBeMoved() {
        return canBeMoved;
    }

    public boolean canBeCrushed() {
        return canBeCrushed;
    }

    public boolean isToxicToPlayer() {
        return toxicToPlayer;
    }
}
