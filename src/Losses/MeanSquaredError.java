package Losses;

import Model.ILoss;

public class MeanSquaredError implements ILoss {

    @Override
    public double Apply(double output, double expectedOutput) {
        double diff = output - expectedOutput;
        return diff * diff;
    }

    @Override
    public double Derivative(double output, double expectedOutput) {
        return 2 * (output - expectedOutput);
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

        return totalError / outputs.length;
    }

    @Override
    public String GetName() {
        return "mean_squared_error";
    }
}
