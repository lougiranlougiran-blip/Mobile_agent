package Model;

public class MathsUtilities {

    public static double Linear(double[] X, double[] W, double b) {
        double r = 0;
        int len = X.length;

        for (int i = 0; i < len; ++i) {
            r += X[i] * W[i];
        }
        return r + b;
    }

    public static int IndexMaxOfArray(double[] arr) {
        int maxIndex = 0;

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
