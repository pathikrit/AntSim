package ants.core.ui;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ImageLoader {
    public static BufferedImage load(String s) {
        try {
            s = "rez/" + s;
            InputStream is = ImageLoader.class.getResourceAsStream(s);
            return ImageIO.read(is);
        } catch (Exception e) {
        }
        throw new RuntimeException("Could not find: " + s);
    }
}
