public class UnflippableDiscException extends Exception { // or RuntimeException for unchecked
    public UnflippableDiscException(String message) {
        super(message); // Call the superclass constructor to set the exception message
    }
}