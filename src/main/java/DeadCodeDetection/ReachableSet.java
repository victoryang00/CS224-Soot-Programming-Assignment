package DeadCodeDetection;

import soot.Unit;
import soot.toolkits.scalar.Pair;

import java.util.*;

public class ReachableSet {
    private final Set<Pair<Unit, Unit>> reachableSet;

    public ReachableSet() {
        this.reachableSet = new HashSet<Pair<Unit, Unit>>();
    }

    public ReachableSet(Set<Pair<Unit, Unit>> objects) {
        this.reachableSet = objects;
    }

    public void add(Unit from, Unit to) {
        reachableSet.add(new Pair<Unit, Unit>(from, to));
    }

    boolean contains(Unit from, Unit to) {
        return reachableSet.contains(new Pair<Unit, Unit>(from, to));
    }
}
