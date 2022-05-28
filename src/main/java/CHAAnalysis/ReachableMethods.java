package CHAAnalysis;

import soot.SootMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReachableMethods {
    private final Set<SootMethod> reachableMethods;
    boolean is_empty;

    public ReachableMethods() {
        is_empty =true;
        this.reachableMethods = new HashSet<SootMethod>();
    }

    public<T> ReachableMethods(Set<T> objects) {
        is_empty =false;
        this.reachableMethods = (Set<SootMethod>) objects;
    }

    public void add(SootMethod method) {
        reachableMethods.add(method);
    }
    public void addAll(Collection<? extends SootMethod> method) {
        reachableMethods.addAll(method);
    }

    boolean contains(SootMethod method) {
        return reachableMethods.contains(method);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (SootMethod method : reachableMethods) {
            res.append(method.toString());
        }
        return res.toString();
    }
}
