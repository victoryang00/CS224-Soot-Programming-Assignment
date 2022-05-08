package LiveVariableAnalysis;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.AbstractBoundedFlowSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.*;

class LiveVariableFlowSet extends AbstractBoundedFlowSet<Local> {

    private final Set<Local> liveVariableSet = new HashSet<>();

    public LiveVariableFlowSet() {
        super();
    }

    @Override
    public LiveVariableFlowSet clone() {
        LiveVariableFlowSet myClone = new LiveVariableFlowSet();
        myClone.liveVariableSet.addAll(this.liveVariableSet);
        return myClone;
    }

    @Override
    public boolean isEmpty() {
        return liveVariableSet.isEmpty();
    }

    @Override
    public int size() {
        return liveVariableSet.size();
    }

    @Override
    public void add(Local local) {
        liveVariableSet.add(local);
    }

    @Override
    public void remove(Local local) {
        liveVariableSet.remove(local);
    }

    @Override
    public boolean contains(Local local) {
        return liveVariableSet.contains(local);
    }

    @Override
    public Iterator<Local> iterator() {
        return liveVariableSet.iterator();
    }

    @Override
    public List<Local> toList() {
        return new ArrayList<>(liveVariableSet);
    }
}

public class LiveVariables extends BackwardFlowAnalysis<Unit, LiveVariableFlowSet> {

    public LiveVariables(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected LiveVariableFlowSet newInitialFlow() {
        return new LiveVariableFlowSet();
    }

    @Override
    protected LiveVariableFlowSet entryInitialFlow() {
        return new LiveVariableFlowSet();
    }


    @Override
    protected void merge(LiveVariableFlowSet srcSet1, LiveVariableFlowSet srcSet2, LiveVariableFlowSet destSet) {
        srcSet1.union(srcSet2, srcSet2);
    }

    @Override
    protected void copy(LiveVariableFlowSet srcSet, LiveVariableFlowSet destSet) {
        srcSet.copy(destSet);

    }
    @Override
    public void doAnalysis() {
        super.doAnalysis();
    }
    @Override
    protected void flowThrough(LiveVariableFlowSet srcSet, Unit u, LiveVariableFlowSet destSet) {
        // kill
        LiveVariableFlowSet kills = new LiveVariableFlowSet();
        for (ValueBox defBox : u.getDefBoxes()) {
            defBox.getValue().apply(new AbstractJimpleValueSwitch() {
                @Override
                public void caseLocal(Local v) {
                    for (Local inValue : srcSet) {
                        if (inValue.equivTo(defBox.getValue())) {
                            kills.add((Local) defBox.getValue());
                        }
                    }
                }
            });
        }
        srcSet.difference(kills, destSet);
        // gen
        for (ValueBox useBox : u.getUseBoxes()) {
            useBox.getValue().apply(new AbstractJimpleValueSwitch() {
                @Override
                public void caseLocal(Local v) {
                    destSet.add((Local) useBox.getValue());
                }
            });
        }
    }
}
