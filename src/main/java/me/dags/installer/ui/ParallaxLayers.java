package me.dags.installer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class ParallaxLayers extends JPanel implements MouseMotionListener, MouseListener {

    private int state = -1;
    private double mouseX = Double.NaN;
    private double mouseY = Double.NaN;

    private final List<ImageLayer> layers = new ArrayList<>();

    public ParallaxLayers() {
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public ParallaxLayers addLayer(ImageLayer layer) {
        layers.add(layer);
        return this;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if (state == -1) {
            state = 0;
            mouseX = this.getWidth() / 2;
            mouseY = this.getHeight() / 2;
            return;
        }

        if (state == 0) {
            int xMiddle = this.getWidth() / 2;
            int yMiddle = this.getHeight() / 2;

            double xInc = mouseX < xMiddle ? 0.4 : mouseX > xMiddle ? -0.4 : 0;
            double yInc = mouseY < yMiddle ? 0.2 : mouseY > yMiddle ? -0.2 : 0;

            mouseX += xInc;
            mouseY += yInc;
        }

        for (ImageLayer layer : layers) {
            int panelCenterX = this.getWidth() / 2, panelCenterY = this.getHeight() / 2;
            int imageCenterX = layer.getWidth() / 2, imageCenterY = layer.getHeight() / 2;

            int xPos = panelCenterX - imageCenterX;
            int yPos = panelCenterY - imageCenterY;

            int xOff = (int) ((this.getWidth() / 2) - mouseX);
            int yOff = (int) ((this.getHeight() / 2) - mouseY);

            int xDrawPos = layer.getMarginLeft() + xPos + (int) (xOff * layer.getFactor());
            int yDrawPos = layer.getMarginTop() + yPos + (int) (yOff * layer.getFactor());

            if (layer.cover()) {
                xDrawPos = clampHorizontal(layer, xDrawPos);
                yDrawPos = clampVertical(layer, yDrawPos);
            }

            g.drawImage(layer.getInstance(), xDrawPos, yDrawPos, this);
        }

        repaint();
    }

    private int clampHorizontal(ImageLayer layer, int xDrawPos) {
        if (xDrawPos > 0) {
            return 0;
        }
        if (xDrawPos + layer.getWidth() < this.getWidth()) {
            return xDrawPos + this.getWidth() - (xDrawPos + layer.getWidth());
        }
        return xDrawPos;
    }

    private int clampVertical(ImageLayer layer, int yDrawPos) {
        if (yDrawPos > 0) {
            return 0;
        }
        if (yDrawPos + layer.getHeight() < this.getHeight()) {
            return yDrawPos + this.getHeight() - (yDrawPos + layer.getHeight());
        }
        return yDrawPos;
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        state = 1;
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        state = 0;
    }
}
