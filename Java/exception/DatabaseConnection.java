package exception;

public class DatabaseConnection extends Exception {
    public DatabaseConnection(String message) {
        super(message);
    }
}