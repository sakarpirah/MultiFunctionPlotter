package plotter;

import java.awt.Color;

public class Util {
    public static Color generateRandomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }
}