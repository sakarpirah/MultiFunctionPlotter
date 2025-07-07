package plotter;

import java.util.regex.*;
import java.util.ArrayList;
import java.awt.Color;

public class Polynomial {
    private String equation;
    private ArrayList<int[]> terms;
    private Color color;

    public Polynomial(String equation) {
        this.equation = equation.replaceAll("\\s+", "");
        this.terms = parsePolynomial(this.equation);
        this.color = generateRandomColor();
    }

    public void setEquation(String equation) {
        this.equation = equation.replaceAll("\\s+", "");
        this.terms = parsePolynomial(this.equation);
    }

    public Color getColor() {
        return color;
    }

    public double evaluate(double x) {
        double y = 0;
        for (int[] term : terms) {
            y += term[0] * Math.pow(x, term[1]);
        }
        return y;
    }

    private ArrayList<int[]> parsePolynomial(String equation) {
        ArrayList<int[]> terms = new ArrayList<>();
        String regex = "([+-]?\\d*)x\\^?(\\d*)|([+-]?\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(equation);

        while (matcher.find()) {
            int coefficient = 0, power = 0;

            if (matcher.group(1) != null) {
                coefficient = matcher.group(1).isEmpty() || matcher.group(1).equals("+") ? 1 : 
                              matcher.group(1).equals("-") ? -1 : Integer.parseInt(matcher.group(1));
                power = matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
            } else if (matcher.group(3) != null) {
                coefficient = Integer.parseInt(matcher.group(3));
                power = 0;
            }
            terms.add(new int[]{coefficient, power});
        }
        return terms;
    }

    private Color generateRandomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }
}