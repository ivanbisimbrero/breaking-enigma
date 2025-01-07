package es.usj.crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import es.usj.crypto.enigma.*;
import es.usj.crypto.enigma.constant.ReflectorConfiguration;
import es.usj.crypto.enigma.constant.RotorConfiguration;

public class HeuristicDecryptor {

    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final Reflector REFLECTOR = new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT);

    public static final String HILLCLIMB_ALGORITHM = "HillClimbing";
    public static final String GENETICAL_ALGORITHM = "Genetical";

    private static final int ROTOR_LEFT = 0;
    private static final int ROTOR_MIDDLE = 1;
    private static final int ROTOR_RIGHT = 2;
    
    private static final int TESTED_PLUGBOARDS = 100000;

    private String encryptedMessage; // Mensaje cifrado
    private List<Rotor[]> rotorConfigurations;
    private List<Rotor[]> bestRotorConfigurations;
    private Entropy machineEntropy;
    private static Set<String> testedPlugboards = HeuristicDecryptor.readGeneratedPlugboards();
    private int pluboardsToTest;
    private String usedAlgorithm;

    public HeuristicDecryptor(String encryptedMessage, String algorithm) {
        this.encryptedMessage = encryptedMessage;
        this.rotorConfigurations = new ArrayList<>();
        this.machineEntropy = new Entropy();
        this.pluboardsToTest = HeuristicDecryptor.TESTED_PLUGBOARDS;
        this.usedAlgorithm = algorithm;
    }

    public HeuristicDecryptor(String encryptedMessage, int pluboardsToTest, String algorithm) {
        this.encryptedMessage = encryptedMessage;
        this.rotorConfigurations = new ArrayList<>();
        this.machineEntropy = new Entropy();
        this.pluboardsToTest = pluboardsToTest;
        this.usedAlgorithm = algorithm;
    }

    public void decrypt() {
        createRotorsCombinations();
        bestRotorConfigurations = chooseBestRotors(encryptedMessage, rotorConfigurations);

        List<String> initialPlugboards = generateRandomPlugboards(this.pluboardsToTest);
        HeuristicDecryptor.writeGeneratedPlugboards(initialPlugboards);

        final EnigmaHeuristic enigmaHeuristic;

        if (this.usedAlgorithm.equals(HeuristicDecryptor.HILLCLIMB_ALGORITHM)) {
            enigmaHeuristic = new HillClimbing(bestRotorConfigurations.get(0), initialPlugboards, encryptedMessage);
        } else {
            enigmaHeuristic = new GeneticalPopulations(initialPlugboards, bestRotorConfigurations.get(0), encryptedMessage);
        }

        enigmaHeuristic.optimize();

        System.out.println("Best Plugboard: " + enigmaHeuristic.getBestPlugboard());
        System.out.println("Best Fitness: " + enigmaHeuristic.getBestFitness());
        System.out.println("Best Decrypted Message: " + enigmaHeuristic.getBestDecryptedMessage());
    
        // Add a ShutdownHook for capturing Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Best Plugboard: " + enigmaHeuristic.getBestPlugboard());
            System.out.println("Best Fitness: " + enigmaHeuristic.getBestFitness());
            System.out.println("Best Decrypted Message: " + enigmaHeuristic.getBestDecryptedMessage());
        }));
    
    }

    public void createRotorsCombinations() {
        List<String> combinations = new ArrayList<>();
        char[] positions = ALPHABET.toCharArray();
        int[] rotorTypes = {1, 2, 3, 4, 5};

        for (int leftRotor : rotorTypes) {
            for (int middleRotor : rotorTypes) {
                for (int rightRotor : rotorTypes) {
                    // Asegurarse de que los rotores sean diferentes
                    if (leftRotor != middleRotor && middleRotor != rightRotor && leftRotor != rightRotor) {
                        for (char leftPosition : positions) {
                            for (char middlePosition : positions) {
                                for (char rightPosition : positions) {
                                    String combination = String.format("L%d-%c M%d-%c R%d-%c",
                                            leftRotor, leftPosition,
                                            middleRotor, middlePosition,
                                            rightRotor, rightPosition);
                                    combinations.add(combination);
                                }
                            }
                        }
                    }
                }
            }
        }
        parseRotorCombinations(combinations);
        System.out.println("Rotors Combinations Generated: " + this.rotorConfigurations.size());
    }

    private void parseRotorCombinations(List<String> combinations) {
        for (String combination : combinations) {
            String[] parts = combination.split(" ");
            Rotor leftRotor = createRotor(parts[HeuristicDecryptor.ROTOR_LEFT]);
            Rotor middleRotor = createRotor(parts[HeuristicDecryptor.ROTOR_MIDDLE]);
            Rotor rightRotor = createRotor(parts[HeuristicDecryptor.ROTOR_RIGHT]);

            this.rotorConfigurations.add(new Rotor[]{leftRotor, middleRotor, rightRotor});
        }
    }

    private static Rotor createRotor(String rotorString) {
        String[] parts = rotorString.split("-");
        int type = Integer.parseInt(parts[0].substring(1));
        char position = parts[1].charAt(0);

        // Here we are using the RotorConfiguration enum to get the rotor configuration
        RotorConfiguration rotorConfiguration = RotorConfiguration.getRotorConfiguration(type);
        return new Rotor(rotorConfiguration, position);
    }

    public List<Rotor[]> chooseBestRotors(String encryptedText, List<Rotor[]> rotorConfigurations) {
        List<CompletableFuture<Pair<Rotor[], Float>>> futures = new ArrayList<>();

        for (Rotor[] rotors : rotorConfigurations) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                String decryptedText = new Machine(
                    new Plugboard(""),
                    rotors[HeuristicDecryptor.ROTOR_LEFT],
                    rotors[HeuristicDecryptor.ROTOR_MIDDLE],
                    rotors[HeuristicDecryptor.ROTOR_RIGHT],
                    HeuristicDecryptor.REFLECTOR
                ).getCipheredText(encryptedText);
                float fitness = machineEntropy.getFitness(decryptedText, true);
                return new Pair<>(rotors, fitness);
            }));
        }

        // Wait for all tasks to complete and collect the results.
        List<Pair<Rotor[], Float>> rotorFitnessPairs = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        // Sort pairs by fitness in descending order
        rotorFitnessPairs.sort(Comparator.<Pair<Rotor[], Float>, Float>comparing(Pair::getSecond).reversed());

        // Extract the ordered rotors
        List<Rotor[]> bestRotors = new ArrayList<>();
        for (Pair<Rotor[], Float> pair : rotorFitnessPairs) {
            bestRotors.add(pair.getFirst());
        }

        return bestRotors.subList(0, Math.min(20, bestRotors.size()));
    }

    private static List<String> generateRandomPlugboards(int count) {
        Set<String> plugboards = Collections.synchronizedSet(new HashSet<>());
        Random random = new Random();
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    
        List<Character> alphabetList = new ArrayList<>();
        for (char c : alphabet) {
            alphabetList.add(c);
        }
    
        int batchSize = 10000;
        while (plugboards.size() < count) {
            int remaining = count - plugboards.size();
            int currentBatchSize = Math.min(batchSize, remaining);
    
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < currentBatchSize; i++) {
                futures.add(CompletableFuture.runAsync(() -> {
                    List<Character> availableChars = new ArrayList<>(alphabetList);
                    Collections.shuffle(availableChars);
    
                    StringBuilder plugboard = new StringBuilder();
                    for (int j = 0; j < 10; j++) {
                        char first = availableChars.remove(random.nextInt(availableChars.size()));
                        char second = availableChars.remove(random.nextInt(availableChars.size()));
                        if (plugboard.length() > 0) {
                            plugboard.append(":");
                        }
                        plugboard.append(first).append(second);
                    }
                    String plugboardStr = plugboard.toString();
                    synchronized (testedPlugboards) {
                        if (!testedPlugboards.contains(plugboardStr)) {
                            plugboards.add(plugboardStr);
                            testedPlugboards.add(plugboardStr);
                        }
                    }
                }));
            }
    
            // Wait until all futures from the batch are completed
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    
        return new ArrayList<>(plugboards);
    }

    private static void writeGeneratedPlugboards(List<String> plugboards) {
        try {
            Files.write(Paths.get("plugboards.txt"), plugboards, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> readGeneratedPlugboards() {
        if(Files.notExists(Paths.get("plugboards.txt"))) {
            return new HashSet<String>();
        } else {
            System.out.println("Found!");
            try {
                List<String> plugboards = Files.readAllLines(Paths.get("plugboards.txt"));
                return new HashSet<String>(plugboards);
            } catch (IOException e) {
                //e.printStackTrace();
                return new HashSet<String>();
            }
        }
    }

}