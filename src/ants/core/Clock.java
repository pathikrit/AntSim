package ants.core;

import java.util.Stack;

public class Clock {
    private static final ThreadLocal<Stack<Clocker>> stack = new ThreadLocal() {
        protected Stack<Clock.Clocker> initialValue() {
            return new Stack();
        }
    };

    public static void in(String s) {
        ((Stack) stack.get()).add(new Clocker(s));
    }

    public static void out() {
        Clocker pop = (Clocker) ((Stack) stack.get()).pop();
        long t = System.nanoTime();
        double millis = (t - pop.time) / 1000000.0D;
        System.out.println(pop.s + ": " + millis + " ms");
    }

    private static class Clocker {
        private final long time = System.nanoTime();
        private final String s;

        public Clocker(String s) {
            this.s = s;
        }
    }
}
