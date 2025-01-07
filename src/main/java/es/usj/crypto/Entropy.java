package es.usj.crypto;

import es.usj.crypto.enigma.Machine;
import es.usj.crypto.fitness.BigramFitness;
import es.usj.crypto.fitness.FitnessFunction;
import es.usj.crypto.fitness.QuadramFitness;
import es.usj.crypto.fitness.SingleCharacterFitness;
import es.usj.crypto.fitness.TrigramFitness;

public class Entropy {

    SingleCharacterFitness unigramFitness;
    BigramFitness bigramFitness;
    TrigramFitness trigramFitness;
    QuadramFitness quadramFitness;

    public Entropy() {
        this.unigramFitness = new SingleCharacterFitness();
        this.bigramFitness = new BigramFitness();
        this.trigramFitness = new TrigramFitness();
        this.quadramFitness = new QuadramFitness();
    }

    public Float getFitness(String decryptedText, boolean rotorsAnalysis) {
        String[] decryptedWords = decryptedText.replace("\n", " ").split(" ");
        float fitness = 0.0f;
        FitnessFunction f;
        if(rotorsAnalysis) {
            for(String word : decryptedWords) {
                if(word.length() == 1) {
                    f = unigramFitness;
                } else {
                    f = bigramFitness;
                }
                fitness += f.score(word.toCharArray());
            }
        } else {
            for(String word : decryptedWords) {
                if(word.length() == 1) {
                    f = unigramFitness;
                } else if(word.length() == 2) {
                    f = bigramFitness;
                } else if(word.length() == 3) {
                    f = trigramFitness;
                } else {
                    f = quadramFitness;
                }
                fitness += f.score(word.toCharArray());
            }
        }
        return fitness;
    }

    public Float getMachineFitness(Machine machine, String encryptedText) {
        return getFitness(machine.getCipheredText(encryptedText), false);
    }
}
