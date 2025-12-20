package Activations;

import Model.IActivation;

public class Sigmoid implements IActivation {

    @Override
    public double Apply(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    @Override
    public double Derivative(double z) {
        double sigmoid = Apply(z);
        return sigmoid * (1 - sigmoid);
    }

    @Override
    public String GetName() {
        return "sigmoid";
    }
}
