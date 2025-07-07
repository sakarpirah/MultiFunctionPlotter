package plotter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class PlotterPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Polynomial> polynomials;
    private double zoomFactor = 1.0;
    private double panX = 0;
    private double panY = 0;
    private int selectedIndex = -1;

    public PlotterPanel() {
        polynomials = new ArrayList<>();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    panX = -(e.getX() - getWidth() / 2.0) / zoomFactor;
                    panY = -(e.getY() - getHeight() / 2.0) / zoomFactor;
                    repaint();
                } else if (e.getClickCount() == 1) {
                    selectPolynomial(e.getX(), e.getY());
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            private int lastX, lastY;

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;

                panX += dx / (zoomFactor);
                panY += dy / (zoomFactor);

                lastX = e.getX();
                lastY = e.getY();

                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            double zoomScale = 1.1;

            if (notches < 0) {
                zoomFactor *= zoomScale;
            } else {
                zoomFactor /= zoomScale;
            }

            repaint();
        });
    }

    public void addPolynomial(String equation) {
        polynomials.add(new Polynomial(equation));
        repaint();
    }

    public void modifySelectedPolynomial(String newEquation) {
        if (selectedIndex != -1 && !newEquation.isEmpty()) {
            polynomials.get(selectedIndex).setEquation(newEquation);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2d.translate(width / 2.0 + panX, height / 2.0 + panY);
        g2d.scale(zoomFactor, zoomFactor);

        g2d.setColor(Color.LIGHT_GRAY);
        int gridSpacing = 50;
        for (int x = -1000000; x <= 1000000; x += gridSpacing) {
            g2d.drawLine(x, -1000000, x, 1000000);
        }
        for (int y = -1000000; y <= 1000000; y += gridSpacing) {
            g2d.drawLine(-1000000, y, 1000000, y);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, -1000000, 0, 1000000);
        g2d.drawLine(-1000000, 0, 1000000, 0);
        
        drawTicksAndLabels(g2d, width, height);

        for (int i = 0; i < polynomials.size(); i++) {
            Polynomial poly = polynomials.get(i);
            g2d.setColor(i == selectedIndex ? Color.RED : poly.getColor());

            for (double x = -10000 / 2.0; x < 10000 / 2.0; x += 0.075) {
                double realX = x / 50.0;
                double y = poly.evaluate(realX);
                int scaledX = (int) (x);
                int scaledY = (int) (-y * 50);

                g2d.fillOval(scaledX, scaledY, 1, 1);
            }
        }
    }
    
    private void drawTicksAndLabels(Graphics2D g2d, int width, int height) {
        int tickSpacing = 1; // Logical units
        int pixelSpacing = 50;

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);

        // X-axis ticks and labels
        for (int x = -1000; x <= 1000; x += tickSpacing) {
            int screenX = (int) (x * pixelSpacing);
            if (x != 0) {
                g2d.drawLine(screenX, -5, screenX, 5);
                g2d.drawString(String.valueOf(x), screenX + 2, 15);
            }
        }

        // Y-axis ticks and labels
        for (int y = -1000; y <= 1000; y += tickSpacing) {
            int screenY = (int) (-y * pixelSpacing);
            if (y != 0) {
                g2d.drawLine(-5, screenY, 5, screenY);
                g2d.drawString(String.valueOf(y), 10, screenY + 4);
            }
        }
    }

    private void selectPolynomial(int mouseX, int mouseY) {
        int width = getWidth();
        int height = getHeight();
        double scaledMouseX = (mouseX - width / 2.0 - panX) / zoomFactor / 50.0;
        double scaledMouseY = -(mouseY - height / 2.0 - panY) / zoomFactor / 50.0;

        for (int i = 0; i < polynomials.size(); i++) {
            Polynomial poly = polynomials.get(i);
            double y = poly.evaluate(scaledMouseX);

            if (Math.abs(y - scaledMouseY) < 0.1) {
                selectedIndex = i;
                repaint();
                return;
            }
        }

        selectedIndex = -1;
        repaint();
    }
}