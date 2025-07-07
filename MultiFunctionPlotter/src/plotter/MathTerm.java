package plotter;

public class MathTerm {
    public int coefficient;
    public int power;
    public String function;
    public String insideFunction;

    public MathTerm(int coefficient, int power) {
        this.coefficient = coefficient;
        this.power = power;
        this.function = null;
        this.insideFunction = null;
    }

    public MathTerm(String function, String insideFunction) {
        this.coefficient = 1;  // Default for function terms
        this.power = 0;
        this.function = function;
        this.insideFunction = insideFunction;
    }
}