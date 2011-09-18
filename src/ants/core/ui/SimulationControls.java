package ants.core.ui;

import ants.core.AntsGameModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class SimulationControls extends JPanel {
    private static final int[] SPEEDS = {1, 2, 4, 8, 16, 32, 64};
    private final AntsGameModel model;
    private JButton pauseButton;
    private JLabel speedLabel;
    private JButton minusButton;
    private JButton plusButton;
    private final ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == SimulationControls.this.minusButton)
                SimulationControls.this.adjustSpeed(-1);
            else if (src == SimulationControls.this.plusButton)
                SimulationControls.this.adjustSpeed(1);
            else if (src == SimulationControls.this.pauseButton)
                SimulationControls.this.pause();
        }
    };

    public SimulationControls(AntsGameModel model) {
        this.model = model;

        initUI();

        initializeListeners();
        syncLabel();
    }

    private void initUI() {
        setLayout(new MigLayout("insets 10, gap 10"));

        this.pauseButton = new JButton("Pause");
        this.speedLabel = new JLabel();
        this.minusButton = new JButton("-");
        this.plusButton = new JButton("+");

        this.speedLabel.setHorizontalAlignment(4);

        add(this.pauseButton, "");
        add(this.speedLabel, "width 150!");
        add(this.minusButton, "");
        add(this.plusButton, "");
    }

    private void initializeListeners() {
        this.minusButton.addActionListener(this.actionListener);
        this.plusButton.addActionListener(this.actionListener);
        this.pauseButton.addActionListener(this.actionListener);
    }

    private void adjustSpeed(int t) {
        int i = 0;
        for (; i < SPEEDS.length; i++) {
            if (SPEEDS[i] == this.model.getSpeed()) {
                break;
            }
        }
        i += t;

        i = Math.max(i, 0);
        i = Math.min(i, SPEEDS.length - 1);

        this.model.setSpeed(SPEEDS[i]);

        syncLabel();
    }

    private void pause() {
        if (this.pauseButton.getText().equals("Pause")) {
            this.model.setPaused(true);
            this.pauseButton.setText("Resume");
        } else {
            this.model.setPaused(false);
            this.pauseButton.setText("Pause");
        }
    }

    private void syncLabel() {
        this.speedLabel.setText(this.model.getSpeed() + "x speed");
    }
}
