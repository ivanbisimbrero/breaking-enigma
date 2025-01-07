package es.usj.crypto;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import es.usj.crypto.enigma.Rotor;
import es.usj.crypto.enigma.Machine;
import es.usj.crypto.enigma.Plugboard;

public class GeneticalPopulations implements EnigmaHeuristic {

    private static final int MAX_GENERATIONS = 100000;

    Entropy entropy;
    List<String> population;
    Set<String> usedPlugboards;
    private Rotor[] rotors;
    private String encryptedMessage;
    private Machine bestMachine;
    private String bestPlugboard;
    private float bestFitness = Float.NEGATIVE_INFINITY;
    private String bestDecryptedMessage;
    private long generation;

    public GeneticalPopulations(List<String> population, Rotor[] rotors, String encryptedMessage) {
        this.entropy = new Entropy();
        this.population = population;
        this.usedPlugboards = new HashSet<String>(population);
        this.generation = 1;
        this.rotors = rotors;
        this.encryptedMessage = encryptedMessage;
    }

    private List<String> selectBestIndividuals() {
        List<CompletableFuture<Pair<String, Float>>> futures = population.stream()
            .map(plugboard -> CompletableFuture.supplyAsync(() -> {
                Machine enigmaMachine = new Machine(new Plugboard(plugboard), rotors[0], rotors[1], rotors[2], HeuristicDecryptor.REFLECTOR);
                float fitness = entropy.getMachineFitness(enigmaMachine, encryptedMessage);
                
                synchronized (this) {
                    if (fitness > bestFitness) {
                        bestMachine = enigmaMachine;
                        bestPlugboard = plugboard;
                        bestFitness = fitness;
                        bestDecryptedMessage = enigmaMachine.getCipheredText(encryptedMessage);
                        System.out.println("New best machine found:");
                        System.out.println("Plugboard: " + bestPlugboard);
                        System.out.println("Score (Fitness): " + bestFitness);
                        System.out.println("Decypted Message: " + bestDecryptedMessage);
                        
                        // Write to the file best.txt
                        writeBestToFile();
                    }
                }
                return new Pair<>(plugboard, fitness);
            }))
            .collect(Collectors.toList());

        // Wait for all futures to complete
        List<Pair<String, Float>> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        // Sort the results by fitness in descending order
        results.sort((p1, p2) -> Float.compare(p2.getSecond(), p1.getSecond()));

        // Return the best individuals
        return results.stream()
            .map(Pair::getFirst)
            .distinct() // Ensure not duplicates
            .collect(Collectors.toList());
    }
     

    private String crossover(String parent1, String parent2) {
        Random random = new Random();
        Set<Character> usedChars = new HashSet<>();
        StringBuilder offspring = new StringBuilder();

        // Cross over the parents
        String[] pairs1 = parent1.split(":");
        String[] pairs2 = parent2.split(":");

        for (int i = 0; i < pairs1.length; i++) {
            String pair = random.nextBoolean() ? pairs1[i] : pairs2[i];
            char first = pair.charAt(0);
            char second = pair.charAt(1);

            // Verify that the characters are not repeated
            if (!usedChars.contains(first) && !usedChars.contains(second)) {
                if (offspring.length() > 0) {
                    offspring.append(":");
                }
                offspring.append(first).append(second);
                usedChars.add(first);
                usedChars.add(second);
            }
        }

        // Ensure the plugboard is completed
        while (usedChars.size() < 20) {
            char first = HeuristicDecryptor.ALPHABET.charAt(random.nextInt(HeuristicDecryptor.ALPHABET.length()));
            char second = HeuristicDecryptor.ALPHABET.charAt(random.nextInt(HeuristicDecryptor.ALPHABET.length()));
            if (first != second && !usedChars.contains(first) && !usedChars.contains(second)) {
                if (offspring.length() > 0) {
                    offspring.append(":");
                }
                offspring.append(first).append(second);
                usedChars.add(first);
                usedChars.add(second);
            }
        }

        return offspring.toString();
    }

    private String mutate(String individual, double mutationRate) {
        Random random = new Random();
        if (random.nextDouble() < mutationRate) {
            // Converts the individual to a list of characters
            String[] pairs = individual.split(":");
            List<Character> currentChars = individual.chars()
                    .filter(c -> c != ':')
                    .mapToObj(c -> (char) c)
                    .collect(Collectors.toList());

            // Find two new characters that are not in the plugboard
            char newChar1, newChar2;
            do {
                newChar1 = HeuristicDecryptor.ALPHABET.charAt(random.nextInt(HeuristicDecryptor.ALPHABET.length()));
            } while (currentChars.contains(newChar1));

            do {
                newChar2 = HeuristicDecryptor.ALPHABET.charAt(random.nextInt(HeuristicDecryptor.ALPHABET.length()));
            } while (newChar1 == newChar2 || currentChars.contains(newChar2));

            // Generate the new pair
            String newPair = "" + newChar1 + newChar2;
            int indexToReplace = random.nextInt(pairs.length);
            pairs[indexToReplace] = newPair;

            // Re-build the individual
            return String.join(":", pairs);
        }
        return individual;
    }

    public void nextGeneration() {
        // Select the best individuals
        List<String> selected = selectBestIndividuals();
    
        // Define an elite percentage
        int eliteSize = (int) (population.size() * 0.05);  // 5% of population
        List<String> elite = selected.subList(0, eliteSize);  // Elige el top 5% como élite
        
        // Create the new generation applying crossover and mutation
        List<String> newPopulation = new ArrayList<>();  // Add the elite without changes
        Random random = new Random();
    
        while (newPopulation.size() < population.size()) {
            String parent1 = elite.get(random.nextInt(elite.size()));
            String parent2 = elite.get(random.nextInt(elite.size()));
            String offspring = crossover(parent1, parent2);
            offspring = mutate(offspring, 0.5);
            if(!usedPlugboards.contains(offspring)) {
                usedPlugboards.add(offspring);
                newPopulation.add(offspring);
            }
        }
    
        // Substitute the old population with the new one
        this.population = newPopulation;
    }

    public void optimize() {
        while (this.generation < GeneticalPopulations.MAX_GENERATIONS) {
            //System.out.println("Generation: " + generation);
            nextGeneration();
            this.generation++;
        }
    }

    public float getBestFitness() {
        return bestFitness;
    }

    public Machine getBestMachine() {
        return bestMachine;
    }

    public String getBestPlugboard() {
        return bestPlugboard;
    }

    public String getBestDecryptedMessage() {
        return bestDecryptedMessage;
    }

    public void writeBestToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("best.txt"))) {
            writer.write("Plugboard: " + bestPlugboard + "\n");
            writer.write("Score (Fitness): " + bestFitness + "\n");
            writer.write("Decrypted Message: " + bestDecryptedMessage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}