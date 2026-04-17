import java.util.concurrent.atomic.AtomicInteger;

public class LabelGen {
    private static final AtomicInteger counter = new AtomicInteger(0);
    public static String newLabel(String prefix) {
        return prefix + "_" + counter.getAndIncrement();
    }
}