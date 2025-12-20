package Activations;

import Model.IActivation;

public class ReLU implements IActivation {

    @Override
    public double Apply(double z) {
        return z > 0 ? z : 0;
    }

    @Override
    public double Derivative(double z) {
        return z > 0 ? 1 : 0;
    }

    @Override
    public String GetName() {
        return "relu";
    }
}
