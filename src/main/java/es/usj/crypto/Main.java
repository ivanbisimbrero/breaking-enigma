package es.usj.crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        String input;
        try {
            input = Files.readString(Path.of("src/main/java/es/usj/crypto/cipher.txt"));
            HeuristicDecryptor decryptor = new HeuristicDecryptor(input, HeuristicDecryptor.HILLCLIMB_ALGORITHM);
            decryptor.decrypt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}