package ants;

public abstract interface Surroundings {
    public abstract Tile getCurrentTile();

    public abstract Tile getTile(Direction paramDirection);
}
