package handlers;

public class InvalidCommandException extends Exception {
    private static final long serialVersionUID = -723003863587251465L;

    public InvalidCommandException(String exp) {
        super(exp);
    }
}
