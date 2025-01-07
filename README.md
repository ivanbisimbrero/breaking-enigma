# 🔐 **Breaking Enigma - Modern Decryption Adventures** 🔍  

Welcome to **Breaking Enigma**, where history meets modern cryptography! This project takes inspiration from Alan Turing's legendary work and adapts it to today’s computational techniques. We tackle the challenge of decrypting a [**custom Enigma machine**](https://github.com/angelborroy/custom-enigma)—no bombs, just code!

---

## 🚀 **Getting Started**

### **Clone the Repository**
```bash
git clone https://github.com/ivanbisimbrero/breaking-enigma.git
cd custom-enigma
```

### **Build the Project with Maven**
```bash
mvn clean package
```

### **Run the Program**
```bash
java -jar target/custom-enigma-0.8.0.jar
```

And now just wait ⏳!! In 20 minutes you should have tested 28M plugboards, with the best result. 🚀

---

## 📜 **What Is This Project About?**  
Imagine being dropped into World War II, but instead of wearing a trench coat, you’re armed with **Java code** and **heuristic algorithms**. This project attempts to **crack encrypted messages** using custom Enigma configurations.  

Our tools? Two powerful algorithms:  

1. **Hill Climbing Algorithm** 🧗‍♂️ - Think of it as climbing a mountain of possibilities to find the peak of decryption.  
2. **Genetic Algorithm** 🧬 - It mimics evolution, creating generations of plugboards until the fittest survives!  

And yes—**300M plugboards** later, we have results to show! 🚀  

---

## 🛠️ **How Does It Work?**  

### 1. **Heuristic Algorithms at Play** 🎲  
- **Hill Climbing**: Tests neighbors, always choosing the best candidate. Efficient but cautious—perfect for focused optimization!  
- **Genetic Algorithm**: Starts with random solutions, breeds and mutates generations to explore larger search spaces.  

### 2. **Fitness Metrics** 📈  
We score decrypted text based on linguistic features (unigrams, bigrams, trigrams, quadgrams). The closer the text matches **natural English**, the better the score.  

### 3. **Combinatorics & Patterns** 🎯  
Using **pairwise combinations**, we analyze plugboard connections and eliminate duplicates, ensuring no character is repeated.  

---

## 💡 **Key Results**  

- **Hill Climbing Wins** 🥇 - Faster convergence, simpler execution, and fewer computational resources made it the ideal choice for this task.  
- **Genetic Algorithm Struggled** 🥈 - While creative, its randomness and complexity didn’t suit the plugboard constraints.  

Final decrypted message? Something deeply philosophical—like unlocking **the secrets of the universe** with a twist of optimism. 🌌✨  

---

## 📂 **Folder Structure**  

- **src** - The Java codebase for heuristic algorithms and configurations.  
- **fitness** - Adapted from [mikepound/enigma](https://github.com/mikepound/enigma), includes text scoring utilities.  
- **data** - Sample inputs, results, and outputs for testing.  
- **docs** - Full walkthrough (because everyone loves documentation, right?).  

---

## 🧩 **Proposed Solution**

The final decrypted message, after evaluating millions of configurations and applying linguistic heuristics, is:

**"IF THOUGHTS INSIDE YOU OF FINDING ARE CLEAR FROM HEAVENS ABOVE AND COMPUTE THE STARS SHINE BRIGHT LIGHT BY KEY INPUT CODE UNVEILING TRUTH KNOWLEDGE CAN INVITE TO SEE HOW TO WORK THE RIVER IS DEEP AND MYSTERY HOLDS MY HOPE SEE WHERE TRUTH HAS EMERGED AND HOLY KEY UNDERSTANDING WILL CONTINUE IT CAN MOVE FORWARD NOW DISCOVERIES REVEALED SHOW A PATH I HOPE TO SEE NEXT FUTURES AWAKENINGS ARE POSSIBILITIES OPENINGS WILL END THOSE WHO LOOK AND SEE I START TO BUILD HOPE FUTURE CHANGES IN A WORLD NEWBEGINNING"**

The process involved analyzing length patterns, semantic relevance, and frequency scoring to select grammatically and contextually appropriate words. Despite the computational complexity, this result demonstrates the power of heuristic methods in solving cryptographic challenges.

---

## 📊 **Highlights**  

- Evaluated more than **300 million plugboards** in total! 
- Used **statistical linguistics** to detect patterns.  
- Decrypted **motivational messages** from the unknown.  

---

## 🧑‍💻 **Author**  

Iván Royo Gutiérrez – [alu.135046@usj.es](mailto:alu.135046@usj.es)  
Universidad San Jorge – Cryptography Project  

---

## 📚 **References**  

- [Cracking Enigma in 2021 - Computerphile](https://www.youtube.com/watch?v=RzWB5jL5RX0)  
- [Mike Pound’s Enigma Project](https://github.com/mikepound/enigma)  
- [Wikipedia - Enigma Machine](https://en.wikipedia.org/wiki/Enigma_machine)
- [Angel Borroy - Custom Enigma Implementation](https://github.com/angelborroy/custom-enigma)

---

## 🎉 **Enjoy the Codebreaking Journey!**  

Feel free to fork, test, and suggest improvements. This project proves that even modern-day codebreakers can relive the excitement of **Breaking Enigma**—one algorithm at a time! 🔓✨
