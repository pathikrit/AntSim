package ants.core.ui;

import ants.core.AntsGameModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class AntsView extends JPanel {
    private final AntsGameModel model;
    private MapView mapView;
    private SimulationControls controls;

    public AntsView(final AntsGameModel model) {
        this.model = model;

        initUI();

        model.addListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();

                if (name.equals(AntsGameModel.PROP_TURN)) {
                    AntsView.this.mapView.repaint();
                } else if (name.equals(AntsGameModel.PROP_GAME_OVER)) {
                    JOptionPane.showMessageDialog(null, "You won after " + model.getTurn() + " turns.");
                    Ants.get().setContentPane(OptionsUI.get());
                }
            }
        });
    }

    private void initUI() {
        setLayout(new MigLayout("insets 0, gap 0"));

        this.mapView = new MapView(this.model.getMap(), this.model.getAntsCarryingFood());
        this.controls = new SimulationControls(this.model);

        add(this.mapView, "width pref!, height pref!, wrap");
        add(this.controls, "width 100%");
    }
}
