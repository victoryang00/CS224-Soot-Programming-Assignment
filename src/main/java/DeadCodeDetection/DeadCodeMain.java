package DeadCodeDetection;

import ConfigTag.ConfigTag;
import DeadCodeDetection.DeadCodeDetection;
import LiveVariableAnalysis.LiveVariables;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.JimpleBody;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.options.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class DeadCodeMain {

    //    public static final String sourceDirectory = System.getProperty("user.dir");
    public static final String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";

    /**
     * reference: https://github.com/PL-Ninja/MySootScript/tree/c4689d06fd08ffad43c810c40398bd44c7705531
     */
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
        Options.v().setPhaseOption("jb.dae", "enabled:false");
        Options.v().setPhaseOption("jb.a", "enabled:false");
        Options.v().setPhaseOption("jb.cp", "enabled:false");
        Options.v().setPhaseOption("jb.ule", "enabled:false");
        Options.v().setPhaseOption("jb.uce", "enabled:false");
        Options.v().setPhaseOption("jb.cp-ule", "enabled:false");
        Options.v().setPhaseOption("jop.cse", "enabled:false");
        Options.v().setPhaseOption("jop.bcm", "enabled:false");
        Options.v().setPhaseOption("jop.lcm", "enabled:false");
        Options.v().setPhaseOption("jop.cp", "enabled:false");
        Options.v().setPhaseOption("jop.cpf", "enabled:false");
        Options.v().setPhaseOption("jop.cbf", "enabled:false");
        Options.v().setPhaseOption("jop.dae", "enabled:false");
        Options.v().setPhaseOption("jop.nce", "enabled:false");
        Options.v().setPhaseOption("jop.uce1", "enabled:false");
        Options.v().setPhaseOption("jop.ubf1", "enabled:false");
        Options.v().setPhaseOption("jop.uce2", "enabled:false");
        Options.v().setPhaseOption("jop.ubf2", "enabled:false");
        Options.v().setPhaseOption("jop.ule", "enabled:false");

        Options.v().set_main_class(classname);
        Scene.v().loadNecessaryClasses();

    }

    protected static void performDeadCodeAnalysis(SootMethod m, String filename, Body b) {
        if (!"<init>".equals(m.getName())) {
            System.out.println("Method : " + m.getName());
            System.out.println("==================");
            DeadCodeDetection dva = new DeadCodeDetection();
            Set<Unit> deadCode = dva.findDeadCode(b);
            deadCode.forEach(System.out::println);
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

                deadCode.forEach(str -> {
                    try {
                        if (str instanceof AssignStmt) {
                            AssignStmt assign = (AssignStmt) str;
                            Value v = assign.getLeftOp();
                            if (v instanceof Local) {
                                if (Objects.equals(v.getType().toString(), "byte")||Objects.equals(v.getType().toString(), "short"))
                                    myWriter.write("Line " + str.getTag(LineNumberTag.IDENTIFIER) + " : " + "int " + str + ";  " + str.getTag(ConfigTag.IDENTIFIER) + ";\n");
                                else
                                    myWriter.write("Line " + str.getTag(LineNumberTag.IDENTIFIER) + " : " + v.getType() + " " + str + ";  " + str.getTag(ConfigTag.IDENTIFIER) + ";\n");
                            } else
                                myWriter.write("Line " + str.getTag(LineNumberTag.IDENTIFIER) + " : " + str + ";  " + str.getTag(ConfigTag.IDENTIFIER) + ";\n");

                        } else {
                            myWriter.write("Line " + str.getTag(LineNumberTag.IDENTIFIER) + " : " + str + ";  " + str.getTag(ConfigTag.IDENTIFIER) + ";\n");
                        }
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                });

                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            System.out.println("==================");
        }
    }

    public static void main(String[] args) {
        if (args.length == 0 || args.length == 1) System.exit(-1);
        Transform transform = new Transform("jtp.DeadCodeDetection", new BodyTransformer() {

            @Override
            protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
                System.out.println("Exiting CFG transformer");
                SootMethod m = Scene.v().getMainClass().getMethodByName(args[1]);
                performDeadCodeAnalysis(m, args[0], b);
            }
        });
        String filename = args[0].replace(".class", "").replace("./", "");
        args[0] = filename;
        setupSoot(filename);
        PackManager.v().getPack("jtp").add(transform);
        JimpleBody body = (JimpleBody) Scene.v().getMainClass().getMethodByName(args[1]).retrieveActiveBody();
        PackManager.v().getPack("jtp").apply(body);

    }
}
