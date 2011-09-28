package ants.core;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AntsGameModel
        implements Runnable {
    public static final String PROP_TURN = AntsGameModel.class.getSimpleName() + ".propTurn";
    public static final String PROP_GAME_OVER = AntsGameModel.class.getSimpleName() + ".propGameOver";
    public static final int WINNING_AMOUNT_OF_FOOD = 500;
    private static final Executor executor = Executors.newSingleThreadExecutor();

    private static final Random rand = new Random();
    private static Point startingLocation;
    private final Class<? extends Ant> antClass;
    private final TileImpl[][] map;
    private final int numTurnsPerNewAnt;
    private final Map<Ant, TileImpl> antTiles = new HashMap();
    private final Set<Ant> antsCarryingFood = new HashSet();
    private int turn = 1;
    private int speed = 1;
    private boolean paused = false;

    private List<PropertyChangeListener> listeners = new LinkedList();

    private Map<Tile, Surroundings> surroundingsCache = new HashMap();
    
    private boolean stressTestMode = false;
    
    public AntsGameModel(Class<? extends Ant> antClass, int mapWidth, int mapHeight, int numStartingAnts, int numTurnsPerNewAnt, boolean stressTestMode) {
        this.stressTestMode = stressTestMode;
        
        this.antClass = antClass;
        this.numTurnsPerNewAnt = numTurnsPerNewAnt;

        this.map = generateMap(mapWidth, mapHeight);

        for (int k = 0; k < numStartingAnts; k++) {
            spawnAnt();
        }

        if (stressTestMode) {
            speed = 128;
        }
        executor.execute(this);        
    }
    

    public AntsGameModel(Class<? extends Ant> antClass, int mapWidth, int mapHeight, int numStartingAnts, int numTurnsPerNewAnt) {
        this(antClass, mapWidth, mapHeight, numStartingAnts, numTurnsPerNewAnt, false);
    }

    public void run() {
        try {
            while (true) {
                if (this.turn % this.numTurnsPerNewAnt == 0) {
                    spawnAnt();
                }

                long now = System.nanoTime();
                if (!this.paused) {
                    if(!stressTestMode) Clock.in("TICK");
                    tick();
                    if(!stressTestMode) Clock.out();

                    if (isWon()) {
                        if(!stressTestMode) System.out.println("You won after " + this.turn + " turns!");
                        firePropertyChange(PROP_GAME_OVER, Boolean.valueOf(false), Boolean.valueOf(true));
                        break;
                    }

                    firePropertyChange(PROP_TURN, Integer.valueOf(this.turn), Integer.valueOf(++this.turn));
                }
                long after = System.nanoTime();
                long idealTickTime = (long) (750.0D / this.speed);
                long actualTime = (long) ((after - now) / 1000000.0D);

                if (actualTime < idealTickTime)
                    Thread.sleep(idealTickTime - actualTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getTurn() {
        return this.turn;
    }

    public Set<Ant> getAntsCarryingFood() {
        return this.antsCarryingFood;
    }

    public void addListener(PropertyChangeListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        this.listeners.remove(listener);
    }

    private void firePropertyChange(String name, Object oldValue, Object newValue) {
        if (this.listeners.isEmpty()) {
            return;
        }
        PropertyChangeEvent e = new PropertyChangeEvent(this, name, oldValue, newValue);
        for (PropertyChangeListener listener : this.listeners)
            listener.propertyChange(e);
    }

    public TileImpl[][] getMap() {
        return this.map;
    }

    public boolean isWon() {
        TileImpl home = getHome();
        return home.getAmountOfFood() >= 500;
    }

    private void tick() {
        for (Map.Entry entry : this.antTiles.entrySet()) {
            Ant ant = (Ant) entry.getKey();
            TileImpl tile = (TileImpl) entry.getValue();
            Surroundings surroundings = (Surroundings) this.surroundingsCache.get(tile);
            if (surroundings == null) {
                surroundings = new SurroundingsImpl(tile, this.map);
                this.surroundingsCache.put(tile, surroundings);
            }

            try {
                if(!stressTestMode) Clock.in("ant logic");
                Surroundings clonedSurroundings = (Surroundings) Serialization.clone(surroundings);
                Action action = ant.getAction(clonedSurroundings);
                if(!stressTestMode) Clock.out();

                if (action == Action.HALT)
                    continue;
                if (action == Action.DROP_OFF) {
                    if (!this.antsCarryingFood.contains(ant)) {
                        System.err.println("An ant cannot drop off food when it is not carrying any!");
                    } else {
                        tile.incrementFood();
                        this.antsCarryingFood.remove(ant);
                    }
                } else if (action == Action.GATHER) {
                    if (this.antsCarryingFood.contains(ant)) {
                        System.err.println("An ant cannot gather when it is already carrying food!");
                    } else if (tile.getAmountOfFood() == 0) {
                        System.err.println("Cannot gather food when there is no food remaining!");
                    } else {
                        tile.decrementFood();
                        this.antsCarryingFood.add(ant);
                    }
                } else {
                    Direction direction = action.getDirection();
                    if (direction == null) {
                        System.err.println("Unrecognized Action: " + action);
                    } else {
                        TileImpl toTile = getTile(tile, direction);
                        if (!toTile.isTravelable()) {
                            System.err.println("Cannot move to a non-travelable tile!");
                        } else {
                            tile.removeAnt(ant);
                            toTile.addAnt(ant, direction);
                            entry.setValue(toTile);

                            if (toTile.getNumAnts() > 1) {
                                byte[] toSend = ant.send();
                                for (Ant ant2 : toTile.getAnts().keySet())
                                    if (ant2 != ant) {
                                        byte[] toSend2 = ant2.send();
                                        ant.receive(toSend2);
                                        ant2.receive(toSend);
                                    }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

    }

    private TileImpl getTile(TileImpl from, Direction direction) {
        int i = from.getI();
        int j = from.getJ();
        if (direction == Direction.NORTH)
            return this.map[i][(j - 1)];
        if (direction == Direction.EAST)
            return this.map[(i + 1)][j];
        if (direction == Direction.SOUTH)
            return this.map[i][(j + 1)];
        if (direction == Direction.WEST) {
            return this.map[(i - 1)][j];
        }
        throw new IllegalArgumentException("UnrecognizedDirection: " + direction);
    }

    private TileImpl[][] generateMap(int width, int height) {
        if ((width <= 0) || (height <= 0)) {
            throw new IllegalArgumentException("width and height must be > 0");
        }

        startingLocation = new Point((int) (1.0D + rand.nextDouble() * (width - 2)), (int) (1.0D + rand.nextDouble() * (height - 2)));

        TileImpl[][] ret = new TileImpl[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                TileImpl t;

                if ((i == startingLocation.x) && (j == startingLocation.y)) {
                    t = new TileImpl(i, j, TileImpl.TileType.HOME, 0);
                } else {

                    if ((i == 0) || (j == 0) || (i == width - 1) || (j == height - 1) || (rand.nextDouble() < 0.16D))
                        t = new TileImpl(i, j, TileImpl.TileType.WATER, 0);
                    else
                        t = new TileImpl(i, j, TileImpl.TileType.GRASS, 0);
                }
                ret[i][j] = t;
            }
        }

        int numLocations = width * height / 16;
        int totalFoodToDistribute = 750;
        int[] foodAmounts = new int[numLocations];
        for (int i = 0; i < foodAmounts.length; i++)
            foodAmounts[i] = (totalFoodToDistribute / numLocations);
        int a;
        int b;
        for (int i = 0; i < 1000; i++) {
            a = (int) (rand.nextDouble() * foodAmounts.length);
            b = (int) (rand.nextDouble() * foodAmounts.length);
            if ((foodAmounts[a] > 0) && (foodAmounts[b] < 100)) {
                foodAmounts[a] -= 1;
                foodAmounts[b] += 1;
            }
        }

        for (int foodAmount : foodAmounts) {
            TileImpl t;
            do {
                a = (int) (rand.nextDouble() * width);
                b = (int) (rand.nextDouble() * height);
                t = ret[a][b];
            }
            while ((!t.isTravelable()) || (t.getAmountOfFood() != 0));
            t.setAmountOfFood(foodAmount);
        }

        return ret;
    }

    private Ant spawnAnt() {
        try {
            TileImpl home = getHome();
            Ant ret = (Ant) this.antClass.newInstance();
            this.antTiles.put(ret, home);
            home.addAnt(ret, Direction.NORTH);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TileImpl getHome() {
        return this.map[startingLocation.x][startingLocation.y];
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
