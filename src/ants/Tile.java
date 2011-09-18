package ants;

public abstract interface Tile {
    public abstract int getAmountOfFood();

    public abstract int getNumAnts();

    public abstract boolean isTravelable();
}
