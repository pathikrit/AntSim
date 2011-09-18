package ants;

public abstract interface Ant {
    public abstract Action getAction(Surroundings paramSurroundings);

    public abstract byte[] send();

    public abstract void receive(byte[] paramArrayOfByte);
}
