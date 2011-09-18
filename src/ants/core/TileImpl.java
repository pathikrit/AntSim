package ants.core;

import ants.Ant;
import ants.Direction;
import ants.Tile;
import ants.core.ui.ImageLoader;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TileImpl implements Tile {
    private static final BufferedImage grass = ImageLoader.load("grass.gif");
    private static final BufferedImage water = ImageLoader.load("water.gif");
    private static final BufferedImage antHill = ImageLoader.load("anthill.gif");
    private static final BufferedImage antEmpty = ImageLoader.load("ant_empty.gif");
    private static final BufferedImage antCarry = ImageLoader.load("ant_carry.gif");

    private static final Color foodColor = new Color(255, 255, 0);
    public static final int SIZE = 32;
    public static final int MAX_NUM_FOOD = 100;
    private static Map<String, BufferedImage> antImageCache = new HashMap();
    private final int i;
    private final int j;
    private final TileType type;
    private int numFood;
    private int numAnts = 0;
    private final transient Map<Ant, Direction> ants = new HashMap();

    public TileImpl(int i, int j, TileType type, int numFood) {
        this.i = i;
        this.j = j;
        this.type = type;
        this.numFood = numFood;
    }

    public synchronized void render(Graphics g, Set<Ant> antsWithFood) {
        int x = this.i * 32;
        int y = this.j * 32;

        if (this.type == TileType.GRASS) {
            g.drawImage(grass, x, y, 32, 32, null);
        } else if (this.type == TileType.WATER) {
            g.drawImage(water, x, y, 32, 32, null);
        } else if (this.type == TileType.HOME) {
            g.drawImage(grass, x, y, 32, 32, null);
            g.drawImage(antHill, x, y, 32, 32, null);
        }

        if (this.numFood > 0) {
            g.setColor(foodColor);
            int foodSize = (int) (1.0D * this.numFood / 100.0D * 32.0D);
            if (this.type == TileType.HOME) {
                foodSize = (int) (1.0D * this.numFood / 500.0D * 32.0D / 2.0D);
            }
            foodSize = Math.max(foodSize, 4);
            g.fillOval(x + (32 - foodSize) / 2, y + (32 - foodSize) / 2, foodSize, foodSize);
        }

        for (Map.Entry entry : this.ants.entrySet()) {
            BufferedImage img = getImage((Ant) entry.getKey(), (Direction) entry.getValue(), antsWithFood.contains(entry.getKey()));
            g.drawImage(img, x, y, 32, 32, null);
        }
    }

    private BufferedImage getImage(Ant ant, Direction direction, boolean hasFood) {
        String key = direction.toString() + hasFood;
        BufferedImage ret = (BufferedImage) antImageCache.get(key);
        if (ret == null) {
            ret = hasFood ? antCarry : antEmpty;
            double rotation = getRotation(direction);
            if (rotation != 0.0D) {
                AffineTransform transform = new AffineTransform();
                transform.rotate(rotation, ret.getWidth() / 2, ret.getHeight() / 2);
                AffineTransformOp op = new AffineTransformOp(transform, 3);
                ret = op.filter(ret, null);
            }
            antImageCache.put(key, ret);
        }
        return ret;
    }

    private double getRotation(Direction direction) {
        if (direction == Direction.NORTH)
            return 0.0D;
        if (direction == Direction.EAST)
            return 1.570796326794897D;
        if (direction == Direction.SOUTH)
            return 3.141592653589793D;
        if (direction == Direction.WEST) {
            return -1.570796326794897D;
        }
        throw new IllegalStateException();
    }

    public int getAmountOfFood() {
        return this.numFood;
    }

    public int getNumAnts() {
        return this.numAnts;
    }

    public boolean isTravelable() {
        return this.type != TileType.WATER;
    }

    public synchronized void addAnt(Ant ant, Direction direction) {
        if (this.ants.containsKey(ant)) {
            throw new IllegalArgumentException("Cannot add an ant to a tile it is already on!");
        }

        this.numAnts += 1;
        this.ants.put(ant, direction);
    }

    public synchronized void removeAnt(Ant ant) {
        if (this.ants.remove(ant) == null) {
            throw new IllegalArgumentException("Could not find the ant to remove: " + ant);
        }
        this.numAnts -= 1;
    }

    public Map<Ant, Direction> getAnts() {
        return Collections.unmodifiableMap(this.ants);
    }

    public void decrementFood() {
        if (this.numFood <= 0) {
            throw new RuntimeException("Cannot decrement food when there is no food left!");
        }
        this.numFood -= 1;
    }

    public void incrementFood() {
        this.numFood += 1;
    }

    public TileType getType() {
        return this.type;
    }

    public int getI() {
        return this.i;
    }

    public int getJ() {
        return this.j;
    }

    public void setAmountOfFood(int foodAmount) {
        this.numFood = foodAmount;
    }

    public static enum TileType {
        HOME, GRASS, WATER;
    }
}
