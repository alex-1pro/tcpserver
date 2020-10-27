package il.ac.hit.project.algorithms;

// thrown in case submarine game matrix is invalid
public class InvalidSubmarineMatrixAlgorithmException extends AlgorithmException {
    public InvalidSubmarineMatrixAlgorithmException() {
        super("Given matrix is not a valid submarine game matrix");
    }
}
