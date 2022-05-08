package IntraConstantPropagation;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class IntraConstantPropagation extends ForwardFlowAnalysis<Unit, ConstantPropagationMap> {

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     * Only consider the Java's binary ops on linear locals.
     *
     * @param graph
     */
    public IntraConstantPropagation(DirectedGraph<Unit> graph) {
        super(graph);
    }

    @Override
    protected void flowThrough(ConstantPropagationMap in, Unit d, ConstantPropagationMap out) {
        copy(in, out);
        if (d instanceof AssignStmt) {
            AssignStmt as = (AssignStmt) d;
            Value left = as.getLeftOp();
            if (left instanceof Local) {
                Local l = (Local) left;
                Value right = as.getRightOp();
                ConstantPropagationValue right_cp = in.compute(right);
                out.put(l, right_cp);
            }
        }
    }

    @Override
    protected ConstantPropagationMap newInitialFlow() {
        return new ConstantPropagationMap();
    }

    @Override
    protected void merge(ConstantPropagationMap in1, ConstantPropagationMap in2, ConstantPropagationMap out) {
        ConstantPropagationMap meet = ConstantPropagationMap.meet(in1, in2);
        copy(meet, out);
    }

    @Override
    protected void copy(ConstantPropagationMap source, ConstantPropagationMap dest) {
        dest.copyFrom(source);
    }

    @Override
    public void doAnalysis() {
        super.doAnalysis();
    }
}
