package ants.core.ui;

import java.awt.Container;
import javax.swing.JFrame;

public class Ants extends JFrame {
    private static Ants instance = null;

    public static Ants get() {
        if (instance == null) {
            instance = new Ants();
        }
        return instance;
    }

    public Ants() {
        super("Ants!");

        setDefaultCloseOperation(3);
        setContentPane(OptionsUI.get());
        pack();
        setResizable(true);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setContentPane(Container contentPane) {
        super.setContentPane(contentPane);
        invalidate();
        validate();
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) throws Exception {
        get();
    }
}
