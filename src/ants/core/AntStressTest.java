package ants.core;

import ants.Ant;
import ants.core.ui.OptionsUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.PriorityQueue;

public class AntStressTest {

    private final static int MAX_TURNS = 1500; // anything above this is ignored
    private final static int RUNS = 10; // run simulation this many times
    private final static int DISCARDS = 2; // discard the top 10 and bottom 10 results for calculating average


    public static void main(String[] args) {
        if (args.length == 0) {
            //args = new String[]{"/home/pathikrit/Downloads/MyAnt.java"};
            args = new String[]{"/home/pathikrit/Documents/Programming/My Projects/Addepar-Ants/src/MyAnt.java"};
        }

        Object result = OptionsUI.compile(args[0]);

        final PriorityQueue<Integer> runs = new PriorityQueue<Integer>(RUNS);

        for (int i = 0; i < 1; i++) {

            final AntsGameModel game = new AntsGameModel((Class<? extends Ant>) result, 20, 20, 3, 10, true);

            game.addListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String state = evt.getPropertyName();
                    int turns = game.getTurn();

                    if (state.equals(AntsGameModel.PROP_GAME_OVER)) {
                        runs.add(turns);
                    } else if (evt.getPropertyName().equals(AntsGameModel.PROP_TURN) && turns > MAX_TURNS) {
                        game.setPaused(true);
                    }
                }
            });
        }

        System.out.println(runs);
    }
}
