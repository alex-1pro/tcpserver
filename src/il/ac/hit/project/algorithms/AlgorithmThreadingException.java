package il.ac.hit.project.algorithms;

// throws in case a threading-related exception occurs in one of Algortihm's methods
public class AlgorithmThreadingException extends AlgorithmException {
    public AlgorithmThreadingException(String message) {
        super("A threading exception has occurred: " + message);
    }
}
