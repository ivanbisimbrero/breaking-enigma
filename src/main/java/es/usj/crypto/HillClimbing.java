package es.usj.crypto;

import es.usj.crypto.enigma.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HillClimbing implements EnigmaHeuristic {
    private Rotor[] rotors;
    private List<String> plugboards;
    private String bestPlugboard;
    private float bestFitness;
    private String bestDecryptedMessage;
    private Entropy entropy;
    private String encryptedMessage;
    private Stack<String> plugboardStack;
    private int numTestedPlugboards;

    public HillClimbing(Rotor[] rotors, List<String> initialPlugboards, String encryptedMessage) {
        this.rotors = rotors;
        this.plugboards = initialPlugboards;
        this.bestPlugboard = "";
        this.entropy = new Entropy();
        this.encryptedMessage = encryptedMessage;
        this.bestFitness = Float.NEGATIVE_INFINITY;
        this.plugboardStack = new Stack<>();
        this.numTestedPlugboards = 0;
    }

    public void optimize() {
        // Initialize the stack with the initial plugboards
        plugboardStack.addAll(plugboards);
        System.out.println("Initializing optimization...");

        while (!plugboardStack.isEmpty()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            String currentPlugboard = plugboardStack.pop();
            //System.out.println("Evaluation neighbours of the plugboard: " + currentPlugboard);
            Pair<String, Float> neighbor = evaluateNeighbors(currentPlugboard);
            //System.out.println("Neighbours evaluated.");

            futures.add(CompletableFuture.runAsync(() -> {
                String neighborPlugboard = neighbor.getFirst();
                float neighborFitness = neighbor.getSecond();
                synchronized (this) {
                    if (neighborFitness > bestFitness) {
                        bestPlugboard = neighborPlugboard;
                        bestFitness = neighborFitness;
                        bestDecryptedMessage = new Machine(new Plugboard(bestPlugboard), rotors[0], rotors[1], rotors[2], HeuristicDecryptor.REFLECTOR).getCipheredText(encryptedMessage);
                        System.out.println("New best machine found:");
                        System.out.println("Plugboard: " + bestPlugboard);
                        System.out.println("Puntuación (Fitness): " + bestFitness);
                        System.out.println("Mensaje descifrado: " + bestDecryptedMessage);
                        this.writeBestToFile();
                        //If a better plugboard is found, we will put it in the stack to see if it can be further improved with the missing letters.
                        plugboardStack.push(neighborPlugboard);
                    }
                }
            }));
            
            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        System.out.println("Plugboards evaluated: " + numTestedPlugboards);
    }

    private Set<Character> extractAvailableChars(String plugboard) {
        Set<Character> usedChars = new HashSet<>();
        for (String pair : plugboard.split(":")) {
            usedChars.add(pair.charAt(0));
            usedChars.add(pair.charAt(1));
        }
        Set<Character> availableChars = new HashSet<>();
        for (char c : HeuristicDecryptor.ALPHABET.toCharArray()) {
            if (!usedChars.contains(c)) {
                availableChars.add(c);
            }
        }
        return availableChars;
    }

    private List<String> generateNeighbors(String currentPlugboard, String pair) {
        List<String> neighbors = new ArrayList<>();
        Set<Character> availableChars = extractAvailableChars(currentPlugboard);
        availableChars.add(pair.charAt(0));
        availableChars.add(pair.charAt(1));
    
        List<Character> availableList = new ArrayList<>(availableChars);
        for (int i = 0; i < availableList.size(); i++) {
            for (int j = i + 1; j < availableList.size(); j++) {
                char c1 = availableList.get(i);
                char c2 = availableList.get(j);
                neighbors.add("" + c1 + c2);
            }
        }
        this.numTestedPlugboards += neighbors.size();
        return neighbors;
    }

    private String replacePair(String plugboard, int index, String newPair) {
        String[] pairs = plugboard.split(":");
        pairs[index] = newPair;
        return String.join(":", pairs);
    }

    public Pair<String, Float> evaluateNeighbors(String plugboard) {
        List<CompletableFuture<Pair<String, Float>>> futures = new ArrayList<>();
        String[] pairs = plugboard.split(":");

        for (int i = 0; i < pairs.length; i++) {
            final int index = i;
            for (String neighbor : generateNeighbors(plugboard, pairs[i])) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    String newPlugboard = replacePair(plugboard, index, neighbor);
                    Machine enigmaMachine = new Machine(new Plugboard(newPlugboard), rotors[0], rotors[1], rotors[2], HeuristicDecryptor.REFLECTOR);
                    float fitness = entropy.getMachineFitness(enigmaMachine, encryptedMessage);
                    return new Pair<>(newPlugboard, fitness);
                }));
            }
        }

        List<Pair<String, Float>> neighborFitness = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        neighborFitness.sort((a, b) -> Float.compare(b.getSecond(), a.getSecond()));
        return neighborFitness.get(0);
    }

    public void writeBestToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("best.txt", true))) {
            writer.write("Plugboard: " + bestPlugboard + "\n");
            writer.write("Score (Fitness): " + bestFitness + "\n");
            writer.write("Decrypted Message: " + bestDecryptedMessage + "\n");
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float getBestFitness() {
        return bestFitness;
    }

    public String getBestPlugboard() {
        return bestPlugboard;
    }

    public String getBestDecryptedMessage() {
        return bestDecryptedMessage;
    }

    public int getNumTestedPlugboards() {
        return numTestedPlugboards;
    }
}