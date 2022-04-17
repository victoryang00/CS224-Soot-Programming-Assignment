package LiveVariableAnalysis;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.util.Collections;

public class LiveVariableAnalysis extends BackwardFlowAnalysis<Unit,FlowSet<Local>> {
    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     * Here to ananlyze the liveness for live variable and do not care about the exception.
     * @param graph
     */

    public static final String path = "test/java";

    public LiveVariableAnalysis(DirectedGraph<Unit> graph) {
        super(graph);
    }

    public static void main(String[] args) {
        initial(path);
        SootClass appclass = Scene.v().loadClassAndSupport("test0");
        System.out.println("the main class is :" + appclass.getName());
        LiveVariableAnalysis analysis = new LiveVariableAnalysis(appclass.getMethodByName("main").getActiveBody().getUnits());

    }

    private static void initial(String path) {
        soot.G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_src_prec(Options.src_prec_java);
        Options.v().set_process_dir(Collections.singletonList(path));
        Options.v().set_keep_line_number(true);
		Options.v().set_whole_program(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_app(true);
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Thread", SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
    }

    @Override
    protected void flowThrough(FlowSet<Local> in, Unit d, FlowSet<Local> out) {

    }

    @Override
    protected FlowSet<Local> newInitialFlow() {
        return null;
    }

    @Override
    protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {

    }

    @Override
    protected void copy(FlowSet<Local> source, FlowSet<Local> dest) {

    }
}