import java.util.*;

import syntaxtree.*;
import visitor.*;


/**
 * P3 - main driver for the project.
 *
 * Behavior:
 *  - Parses stdin using the JavaCC/JTB-generated parser (MiniJavaParser.Goal())
 *  - Runs MJClassCollector to produce class metadata (prints to stderr)
 *  - Runs MJIRGenerator to produce an IR.Program
 *  - Prints textual MiniIR to STDOUT using IRPrinter
 *
 * Usage:
 *   java P3 < input/MyProgram.java > output/MyProgram.miniIR 2> output/MyProgram.txt
 */
public class P3 {
    public static void main(String[] args) {
        try {
            //println("[P3] Starting parse of stdin...");

            // parse input (adjust class name if your parser is named differently)
            Goal root = null;
            try {
                root = new MiniJavaParser(System.in).Goal();
            } catch (NoClassDefFoundError | Exception e) {
                //println("[P3] Could not instantiate MiniJavaParser. Please change the parser invocation in P3.java to match your parser class name.");
                //e.printStackTrace();
                System.exit(1);
            }

            //println("[P3] Parse completed.");

            // 1) Class collection
            MJClassCollector collector = new MJClassCollector();
            root.accept(collector, null); // use GJDepthFirst visitor pattern

            Map<String, ClassInfo> classTable = collector.getClassTable();

            //println("=== CLASS TABLE ===");
            for (ClassInfo ci : classTable.values()) {
                //println(ci.toDetailedString());
            }
            //println("===================");

            //println("[P3] Class collection done. Running IR generator...");

            // 2) IR generation (uses classTable)
            MJIRGenerator gen = new MJIRGenerator(classTable);
            root.accept(gen, null);

            gen.finalizeProcedures();

            IR.Program prog = gen.getProgram();

            //println("[P3] IR generation complete. Emitting MiniIR to stdout...");

            // 3) Print IR to stdout
            IRPrinter printer = new IRPrinter();
            printer.printProgram(prog);

            //println("[P3] Done.");

        } catch (Throwable t) {
            //println("[P3] Fatal error:");
            //t.printStackTrace();
            System.exit(1);
        }
    }

    
}
