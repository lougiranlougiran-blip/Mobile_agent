package Activations;

import java.util.Arrays;

import Model.IActivation;

public class SoftMax implements IActivation {

    @Override
    public double Apply(double z) {
        return 0;
    }

    @Override
    public double Derivative(double z) {
        return 0;
    }

    private double[] ApplyArr(double[] z) {
        double max = Arrays.stream(z).max().orElse(0.01);
        double sum = 0;
        double[] res = new double[z.length];

        for (int i = 0; i < z.length; i++) {
            res[i] = Math.exp(z[i] - max);
            sum += res[i];
        }

        for (int i = 0; i < z.length; i++) {
            res[i] /= sum;
        }

        return res;
    }

    private double[] DerivativeArr(double[] z) {
        double[] softmax = ApplyArr(z);
        double[] res = new double[z.length];

        for (int i = 0; i < z.length; i++) {
            res[i] = softmax[i] * (1 - softmax[i]);
        }

        return res;
    }

    @Override
    public double[][] ApplyMatrix(double[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            result[i] = ApplyArr(input[i]);
        }

        return result;
    }

    @Override
    public double[][] DerivativeMatrix(double[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            result[i] = DerivativeArr(input[i]);
        }

        return  result;
    }

    @Override
    public String GetName() {
        return "softmax";
    }
}
