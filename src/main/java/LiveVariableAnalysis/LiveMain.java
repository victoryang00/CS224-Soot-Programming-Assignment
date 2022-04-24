package LiveVariableAnalysis;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;  // Import the IOException class to handle errors

import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;


public class LiveMain {

    public static final String sourceDirectory = System.getProperty("user.dir");
//    public static final String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
/** reference: https://github.com/PL-Ninja/MySootScript/tree/c4689d06fd08ffad43c810c40398bd44c7705531 */
    public static void setupSoot(String classname) {
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_output_dir("./output");
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_whole_program(true);
        Options.v().set_verbose(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.dae", "only-stack-locals:true");

        Options.v().set_main_class(classname);
        Scene.v().loadNecessaryClasses();

    }

    protected static void performLiveVariablesAnalysis(SootMethod m, String filename) {
        if (!"<init>".equals(m.getName())) {
            System.out.println("Method : " + m.getName());
            System.out.println("==================");
            JimpleBody body = (JimpleBody) m.retrieveActiveBody();
            BriefUnitGraph g = new BriefUnitGraph(body);
            LiveVariables lva = new LiveVariables(g);
            try {
                File myPath = new File("output/");
                if (!myPath.exists()) {
                    if (myPath.mkdir()) {
                        System.out.println("Path created.");
                    }
                }
                File myObj = new File("output/" + filename + ".txt");
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            try {
                FileWriter myWriter = new FileWriter("output/" + filename + ".txt");

                for (Unit unit : body.getUnits()) {
                    Unit unit1 = body.getUnits().getSuccOf(unit);
                    if (unit1 != null) {
//                    System.out.println(unit.toString() + "; "+  lva.getFlowBefore(unit1).toString().replace("{","[").replace("}","]") );
                        myWriter.write(unit.toString() + "; " + lva.getFlowBefore(unit1).toString().replace("{", "[").replace("}", "]") + "\n");
                    }
                }
                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            System.out.println("==================");
        }
    }

    private static void printCallGraph(CallGraph cg, SootMethod m) {
        Iterator targets = new Targets(cg.edgesOutOf(m));
        while (targets.hasNext()) {
            SootMethod trgt = (SootMethod) targets.next();
            //System.out.println(m.getName() + " -> " + trgt.getName() + ";");
            performLiveVariablesAnalysis(trgt, "");
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) System.exit(-1);
        PackManager.v().getPack("wjtp").
                add(new Transform("wjtp.liveVariables", new SceneTransformer() {

                    @Override
                    protected void internalTransform(String phaseName, Map options) {
                        CHATransformer.v().transform();
                        CallGraph cg = Scene.v().getCallGraph();
                        System.out.println("Exiting CFG transformer");
                        SootMethod m = Scene.v().getMainClass().getMethodByName("main");
                        performLiveVariablesAnalysis(m, args[0]);
                        printCallGraph(cg, m);
                    }
                }));
        String filename = args[0].replace(".class", "").replace("./", "");
        args[0] = filename;
        setupSoot(filename);
        SootMethod m = Scene.v().getMainClass().getMethodByName("main");
        performLiveVariablesAnalysis(m, filename);
        soot.Main.main(args);
    }
}