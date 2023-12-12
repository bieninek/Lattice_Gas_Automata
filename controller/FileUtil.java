package controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileUtil {
    public void saveToFile(BufferedImage img) throws IOException {
        ImageIO.write(img, "bmp",
                new File("src\\input.png"));
    }

    public BufferedImage openFromFile() {
        try {
            return ImageIO.read(new File("src\\input.png"));
        } catch (IOException ex) {
            System.exit(-1);
            return null;
        }
    }
}
