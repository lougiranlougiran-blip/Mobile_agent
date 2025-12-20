package Losses;

import Model.ILoss;

public class CrossEntropy implements ILoss {

    private static final double EPSILON = 1E-9;

    @Override
    public double Apply(double output, double expectedOutput) {
        return -expectedOutput * Math.log(output + EPSILON);
    }

    @Override
    public double Derivative(double output, double expectedOutput) {
        if (output == 0 || output == 1) {
            return 0;
        }
        return (-output + expectedOutput) / (output * (output - 1));
    }

    @Override
    public double Loss(double[] output, double[] expectedOutputs) {
        double error = 0;

        for (int numOutput = 0; numOutput < output.length; numOutput++) {
            error += Apply(output[numOutput], expectedOutputs[numOutput]);
        }

        return error;
    }

    @Override
    public double GlobalLoss(double[][] outputs, double[][] expectedOutputs) {
        double totalError = 0;

        for (int i = 0; i < outputs.length; i++) {
            totalError += Loss(outputs[i], expectedOutputs[i]);
        }

        return totalError;
    }

    @Override
    public String GetName() {
        return "cross_entropy";
    }
}
