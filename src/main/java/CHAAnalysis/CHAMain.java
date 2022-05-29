package CHAAnalysis;

import com.sun.org.apache.xerces.internal.xs.LSInputList;
import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CHAMain {

    public static final String sourceDirectory = System.getProperty("user.dir");
//    public static final String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";

    /**
     * reference: <a href="https://github.com/PL-Ninja/MySootScript/tree/c4689d06fd08ffad43c810c40398bd44c7705531">https://github.com/PL-Ninja/MySootScript/tree/c4689d06fd08ffad43c810c40398bd44c7705531</a>
     */
    public static void setupSoot(String classname) {
        LinkedList<String> excluded_class = new LinkedList<>();
        excluded_class.add("CHAInput3");
        excluded_class.add("CHAInput1");
        excluded_class.add("CHAInput2");
        excluded_class.add("A");
        excluded_class.add("B");
        excluded_class.add("C");
        excluded_class.add("D");
        excluded_class.add("E");
        excluded_class.add("CHAAnalysis.*");
        excluded_class.add("ConfigTag.*");
        excluded_class.add("DeadCodeDetection.*");
        excluded_class.add("IntraConstantPropagation.*");
        excluded_class.add("LiveVariableAnalysis.*");
        excluded_class.add("DeadCodeInput");
        excluded_class.add("DeadCodeInput1");
        excluded_class.add("DeadCodeInput2");
        excluded_class.add("DeadCodeInput3");
        excluded_class.add("LiveVariablesInput");
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
        Options.v().set_exclude(excluded_class);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.dae", "only-stack-locals:true");

        Options.v().set_main_class(classname);
        Scene.v().loadNecessaryClasses();

    }

    protected static void performCHAAnalysis(String filename) {
        CHAAnalysis cha = CHAAnalysis.analysis;
        Chain<SootClass> a = Scene.v().getApplicationClasses();
        cha.resolve(a);
        String toOutput = "";
        String toOutputLast = "";
        List<String> toOutputLastList = new LinkedList<>();
        List<Integer> toCountLast = new LinkedList<>();
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            if (clazz.getName().contains(filename)) {
                for (SootMethod method : clazz.getMethods()) {
                    if (cha.reachableSets.contains(method)) {
                        toOutputLast = "";
                        int edgecount = 0;
                        toOutput += method.getSignature() + "\n";
                        toOutputLast += method.getSignature() + ":\n";
                        Set<CHACallNode> edgeSet = cha.getCallout(method);
                        for (CHACallNode callEdge : edgeSet) {
                            toOutputLast += callEdge + "\n";
                            edgecount++;
                        }
                        toOutputLastList.add(toOutputLast);
                        toCountLast.add(edgecount);
                    }
                }
            }
        }
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
            myWriter.write("#reacheable method: " + toCountLast.size() + "\n");
            myWriter.write(toOutput + "\n");
            myWriter.write("#call graph edges: " + toCountLast.get(0) + "\n");
            myWriter.write(toOutputLastList.get(0) + "\n");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println("==================");

    }

    public static void main(String[] args) {
        if (args.length == 0) System.exit(-1);
        Transform transform = new Transform("wjtp.CHAAnalysis", new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> options) {
                performCHAAnalysis(args[0]);
            }
        });
        String filename = args[0].replace(".class", "").replace("./", "");
        args[0] = filename;
        setupSoot(filename);
        PackManager.v().getPack("wjtp").add(transform);
        soot.Main.main(args);
    }
}