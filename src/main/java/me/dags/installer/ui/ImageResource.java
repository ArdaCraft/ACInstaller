package me.dags.installer.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class ImageResource {

    private final BufferedImage bufferedImage;

    private Image instance;
    private int currentWidth;
    private int currentHeight;

    public ImageResource(String path) throws IOException {
        bufferedImage = ImageIO.read(ImageResource.class.getResource(path));
        instance = bufferedImage;
        currentWidth = bufferedImage.getWidth();
        currentHeight = bufferedImage.getHeight();
    }

    public int getActualHeight() {
        return bufferedImage.getHeight();
    }

    public int getActualWidth() {
        return bufferedImage.getWidth();
    }

    public int getHeight() {
        return currentHeight;
    }

    public int getWidth() {
        return currentWidth;
    }

    public ImageResource scale(double factor) {
        int width = (int) (getWidth() * factor);
        int height = (int) (getHeight() * factor);
        updateSize(width, height);
        return this;
    }

    public ImageResource updateSize(int width, int height) {
        if (width != currentWidth || height != currentHeight) {
            resize(width, height);
        }
        return this;
    }

    public Image resize(int width, int height) {
        return instance = bufferedImage.getScaledInstance(currentWidth = width, currentHeight = height, Image.SCALE_SMOOTH);
    }

    public Image getInstance() {
        return instance;
    }
}
