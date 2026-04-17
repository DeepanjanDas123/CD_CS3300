package visitor;

public class ErrorHandler {
    private static boolean errorReported = false;

    public static void report(String message) {
        if (!errorReported) {
            System.err.println(message);
            errorReported = true;
            System.exit(1);  // stop compilation after first error
        }
    }
}
