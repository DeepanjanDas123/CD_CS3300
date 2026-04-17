import java.util.concurrent.atomic.AtomicInteger;

public class TempGen {
    private static final AtomicInteger counter = new AtomicInteger(0);
    public static int next() { return counter.getAndIncrement(); }
    public static String temp(int t) { return "TEMP " + t; }
}
