package es.usj.crypto;

public interface EnigmaHeuristic {

    String bestPlugboard = "";
    float bestFitness = 0.0f;
    String bestDecryptedMessage = "";

    void optimize();

    float getBestFitness();
    String getBestPlugboard();
    String getBestDecryptedMessage();

    void writeBestToFile();

}
