package ants.core;

import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class SurroundingsImpl implements Surroundings {
    private final Tile current;
    private final Tile north;
    private final Tile east;
    private final Tile south;
    private final Tile west;

    public SurroundingsImpl(TileImpl tile, TileImpl[][] map) {
        this(tile, map[tile.getI()][(tile.getJ() - 1)], map[(tile.getI() + 1)][tile.getJ()], map[tile.getI()][(tile.getJ() + 1)],
                map[(tile.getI() - 1)][tile.getJ()]);
    }

    public SurroundingsImpl(Tile current, Tile north, Tile east, Tile south, Tile west) {
        this.current = current;
        this.north = north;
        this.east = east;
        this.south = south;
        this.west = west;
    }

    public Tile getCurrentTile() {
        return this.current;
    }

    public Tile getTile(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction is null");
        }

        if (direction == Direction.NORTH)
            return this.north;
        if (direction == Direction.EAST)
            return this.east;
        if (direction == Direction.SOUTH)
            return this.south;
        if (direction == Direction.WEST) {
            return this.west;
        }
        throw new IllegalStateException("Unknown direction: " + direction);
    }
}
