package ants.core.ui;

import ants.Ant;
import ants.core.TileImpl;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Set;
import javax.swing.JPanel;

public class MapView extends JPanel {
    private final TileImpl[][] map;
    private final BufferedImage buffer;
    private final Set<Ant> antsWithFood;

    public MapView(TileImpl[][] map, Set<Ant> antsWithFood) {
        this.map = map;
        this.antsWithFood = antsWithFood;

        Dimension dim = new Dimension(map.length * 32, map[0].length * 32);
        setPreferredSize(dim);
        this.buffer = new BufferedImage(dim.width, dim.height, 2);
    }

    protected void paintComponent(Graphics g) {
        render(g);
    }

    public void render() {
        Graphics g = getGraphics();
        render(g);
        g.dispose();
    }

    private void render(Graphics gg) {
        Graphics2D g = this.buffer.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                TileImpl tile = this.map[i][j];
                tile.render(g, this.antsWithFood);
            }
        }

        g.dispose();

        gg.drawImage(this.buffer, 0, 0, null);
    }
}
