package es.usj.crypto.fitness;

public class IoCFitness extends FitnessFunction {

    public IoCFitness() {
    }

    @Override
    public float score(char[] text) {
        int[] histogram = new int[26];
        for (char c : text) {
            histogram[c - 65]++;
        }

        int n = text.length;
        float total = 0.0f;

        for (int v : histogram) {
            total += (v * (v - 1));
        }

        return total / (n * (n-1));
    }
}
