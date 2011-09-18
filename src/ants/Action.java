package ants;

public class Action {
    public static Action HALT = new Action();

    public static Action GATHER = new Action();

    public static Action DROP_OFF = new Action();
    private final Direction direction;

    public static Action move(Direction direction) {
        return new Action(direction);
    }

    private Action() {
        this(null);
    }

    private Action(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return this.direction;
    }
}
