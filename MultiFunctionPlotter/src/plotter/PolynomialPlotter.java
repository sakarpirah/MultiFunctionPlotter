package plotter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolynomialPlotter extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> equations;
    private List<Color> colors;
    private double zoomFactor = 1.0;
    private double panX = 0;
    private double panY = 0;
    private int selectedIndex = -1;

    public PolynomialPlotter() {
        equations = new ArrayList<>();
        colors = new ArrayList<>();

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
        equations.add(equation.replaceAll("\\s+", ""));
        colors.add(generateRandomColor());
        repaint();
    }

    public void modifySelectedPolynomial(String newEquation) {
        if (selectedIndex != -1 && !newEquation.isEmpty()) {
            equations.set(selectedIndex, newEquation.replaceAll("\\s+", ""));
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

        for (int i = 0; i < equations.size(); i++) {
            String equation = equations.get(i);
            ArrayList<MathTerm> terms = parseEquation(equation);

            g2d.setColor(i == selectedIndex ? Color.RED : colors.get(i));

            for (double x = -10000 / 2.0; x < 10000 / 2.0; x += 0.075) {
                double realX = x / 50.0;
                double y = evaluateExpression(terms, realX);
                int scaledX = (int) (x);
                int scaledY = (int) (-y * 50);

                g2d.fillOval(scaledX, scaledY, 1, 1);
            }
        }
    }

    private void drawTicksAndLabels(Graphics2D g2d, int width, int height) {
        int tickSpacing = 1;
        int pixelSpacing = 50;

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);

        for (int x = -1000; x <= 1000; x += tickSpacing) {
            int screenX = (int) (x * pixelSpacing);
            if (x != 0) {
                g2d.drawLine(screenX, -5, screenX, 5);
                g2d.drawString(String.valueOf(x), screenX + 2, 15);
            }
        }

        for (int y = -1000; y <= 1000; y += tickSpacing) {
            int screenY = (int) (-y * pixelSpacing);
            if (y != 0) {
                g2d.drawLine(-5, screenY, 5, screenY);
                g2d.drawString(String.valueOf(y), 10, screenY + 4);
            }
        }
    }

    private ArrayList<MathTerm> parseEquation(String equation) {
        ArrayList<MathTerm> terms = new ArrayList<>();
        String regex = "([+-]?\\d*)x\\^?(\\d*)|([+-]?\\d+)|(sin|cos|tan|log|ln)\\((.*)\\)"; // Updated regex
        Matcher matcher = Pattern.compile(regex).matcher(equation);

        while (matcher.find()) {
            int coefficient = 0, power = 0;
            String function = matcher.group(4);
            String insideFunction = matcher.group(5);

            if (function != null) {
                terms.add(new MathTerm(function, insideFunction));
            } else {
                if (matcher.group(1) != null) {
                    coefficient = matcher.group(1).isEmpty() || matcher.group(1).equals("+") ? 1 :
                            matcher.group(1).equals("-") ? -1 : Integer.parseInt(matcher.group(1));
                    power = matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
                } else if (matcher.group(3) != null) {
                    coefficient = Integer.parseInt(matcher.group(3));
                    power = 0;
                }
                terms.add(new MathTerm(coefficient, power));
            }
        }
        return terms;
    }

    private double evaluateExpression(ArrayList<MathTerm> terms, double x) {
        double y = 0;
        for (MathTerm term : terms) {
            if (term.function != null) {
                if (term.function.equals("sin")) {
                    y += Math.sin(x);
                } else if (term.function.equals("cos")) {
                    y += Math.cos(x);
                } else if (term.function.equals("tan")) { // Added case for tan
                    y += Math.tan(x);
                } else if (term.function.equals("log")) {
                    y += Math.log10(x);
                } else if (term.function.equals("ln")) {
                    if (x > 0) {
                        y += Math.log(x);
                    } else {
                        y += 0; // Handle invalid domain for logarithmic
                    }
                }
            } else {
                y += term.coefficient * Math.pow(x, term.power);
            }
        }
        return y;
    }

    private void selectPolynomial(int mouseX, int mouseY) {
        int width = getWidth();
        int height = getHeight();
        double scaledMouseX = (mouseX - width / 2.0 - panX) / zoomFactor / 50.0;
        double scaledMouseY = -(mouseY - height / 2.0 - panY) / zoomFactor / 50.0;

        for (int i = 0; i < equations.size(); i++) {
            ArrayList<MathTerm> terms = parseEquation(equations.get(i));

            for (double x = scaledMouseX - 0.1; x <= scaledMouseX + 0.1; x += 0.01) {
                double y = evaluateExpression(terms, x);

                if (Math.abs(y - scaledMouseY) < 0.1) {
                    selectedIndex = i;
                    repaint();
                    return;
                }
            }
        }
        selectedIndex = -1;
        repaint();
    }

    private Color generateRandomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Polynomial and Function Plotter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        PolynomialPlotter plotter = new PolynomialPlotter();

        JPanel controlPanel = new JPanel(new FlowLayout());

        JTextField inputField = new JTextField(20);
        JButton addButton = new JButton("Add Function");
        addButton.addActionListener(e -> {
            String equation = inputField.getText();
            if (!equation.isEmpty()) {
                plotter.addPolynomial(equation);
                inputField.setText("");
            }
        });

        JTextField modifyField = new JTextField(20);
        JButton modifyButton = new JButton("Modify Function");
        modifyButton.addActionListener(e -> {
            String newEquation = modifyField.getText();
            plotter.modifySelectedPolynomial(newEquation);
            modifyField.setText("");
        });

        controlPanel.add(inputField);
        controlPanel.add(addButton);
        controlPanel.add(modifyField);
        controlPanel.add(modifyButton);

        frame.setLayout(new BorderLayout());
        frame.add(plotter, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}