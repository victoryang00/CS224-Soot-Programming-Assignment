package DeadCodeDetection;

import java.util.*;

import IntraConstantPropagation.*;
import LiveVariableAnalysis.LiveVariables;
import soot.jimple.*;
import soot.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import ConfigTag.ConfigTag;

public class DeadCodeDetection {
    boolean marked_case_stmt =false;
    public Set<Unit> findDeadCode(Body b) {
        b = (Body) b.clone();
        Set<Unit> deadCode = new HashSet<Unit>();

        int prevSize = -1;
        int count = 0;
        while (prevSize != deadCode.size()) {
            count++;
            if (marked_case_stmt)
            if (count == 2) {
                break;
            }
            prevSize = deadCode.size();
            Set<Unit> deadAssignments = findDeadAssign(b);
            deadAssignments.forEach(u -> {
                u.addTag(new ConfigTag("dead assignment"));
            });
            UnitGraph cfg = new BriefUnitGraph(b);
            Set<Unit> unreachable = findUnreachable(b, cfg);
            deadCode.addAll(deadAssignments);
            deadCode.addAll(unreachable);

            b.getUnits().removeIf(deadCode::contains);
        }
        Body body = (Body) b.clone();
        body.getUnits().removeIf(deadCode::contains);

        return deadCode;
    }

    private Set<Unit> findUnreachable(Body b, UnitGraph cfg) {
        IntraConstantPropagation icp = new IntraConstantPropagation(cfg);
        icp.doAnalysis();
        UnitPatchingChain unit_cfg = b.getUnits();
        ReachableSet unreachable = new ReachableSet();
        Set<Unit> unreachable_edges = new HashSet<Unit>();
        Set<Unit> visited = new HashSet<Unit>();
        Queue<Unit> queue = new LinkedList<Unit>();
        queue.add(cfg.getHeads().get(0));

        /** Do propagation to ifstmt */
        for (Unit unit : unit_cfg) {
            if (unit instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) unit;
                Value v = ifStmt.getCondition();
                ConstantPropagationMap map = icp.getFlowBefore(ifStmt);
                ConstantPropagationValue cp_value = map.compute(v);
                if (cp_value != ConstantPropagationValue.UNDEF && cp_value != ConstantPropagationValue.NAC) {
                    if (cp_value.concrete_val == 0) {
                        unreachable.add(ifStmt, ifStmt.getTarget());
                    } else if (cp_value.concrete_val == 1) {
                        unreachable.add(ifStmt, b.getUnits().getSuccOf(ifStmt));
                    }
                }
            } else if (unit instanceof SwitchStmt) {
                marked_case_stmt =true;
                SwitchStmt switchStmt = (SwitchStmt) unit;
                Value v = switchStmt.getKey();
                ConstantPropagationMap map = icp.getFlowBefore(switchStmt);
                ConstantPropagationValue cp_value = map.compute(v);
                if (cp_value != ConstantPropagationValue.UNDEF && cp_value != ConstantPropagationValue.NAC) {
                    for (int i = 0; i < switchStmt.getTargets().size(); i++) {
                        if (i != cp_value.concrete_val - 1) {
                            Unit ith_stmt = switchStmt.getTarget(i);
                            if (ith_stmt instanceof GotoStmt)
                                ith_stmt.addTag(new ConfigTag("fall through"));
                            else
                                ith_stmt.addTag(new ConfigTag("unreachable branch"));
                            unreachable_edges.add(ith_stmt);
                        }
                    }
                    Unit default_stmt = switchStmt.getDefaultTargetBox().getUnit();
                    if (default_stmt instanceof GotoStmt)
                        default_stmt.addTag(new ConfigTag("fall through"));
                    else
                        default_stmt.addTag(new ConfigTag("unreachable branch"));
                    unreachable_edges.add(default_stmt);
                }
            } else if (unit instanceof GotoStmt) {
                GotoStmt gotoStmt = (GotoStmt) unit;
                if (marked_case_stmt) {
                    gotoStmt.addTag(new ConfigTag("fall through"));
                    unreachable_edges.add(gotoStmt);
                }
            }
        }
        /** Transform the branch to code edges */
        while (!queue.isEmpty()) {
            Unit curr = queue.poll();
            if (visited.contains(curr)) {
                continue;
            }
            visited.add(curr);
            List<Unit> successor = cfg.getSuccsOf(curr);
            for (Unit succ : successor) {
                if (!unreachable.contains(curr, succ)) {
                    queue.add(succ);
                }
            }
        }
        for (Unit unit : cfg) {
            if (!visited.contains(unit)) {
                unit.addTag(new ConfigTag("unreachable branch"));
                unreachable_edges.add(unit);
            }
        }

        /** Do propagation to returnstmt */
        unit_cfg = b.getUnits();
        unreachable = new ReachableSet();
        visited = new HashSet<Unit>();
        queue = new LinkedList<Unit>();
        queue.add(cfg.getHeads().get(0));
        for (Unit unit : unit_cfg) {
            if (unit instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) unit;
                Unit u = b.getUnits().getPredOf(returnStmt);
                if (u instanceof IfStmt) {
                    IfStmt ifStmt = (IfStmt) u;
                    Value v = ifStmt.getCondition();
                    ConstantPropagationMap map = icp.getFlowBefore(ifStmt);
                    ConstantPropagationValue cp_value = map.compute(v);
                    if (cp_value != ConstantPropagationValue.UNDEF && cp_value != ConstantPropagationValue.NAC) {
                        if (cp_value.concrete_val == 1) {
                            unreachable.add(returnStmt, b.getUnits().getLast());
                        }
                    }
                } else {
                    unreachable.add(returnStmt, b.getUnits().getLast());
                }
            }
        }
        /** Transform the branch to code edges */
        while (!queue.isEmpty()) {
            Unit curr = queue.poll();
            if (visited.contains(curr)) {
                continue;
            }
            visited.add(curr);
            List<Unit> successor = cfg.getSuccsOf(curr);
            for (Unit succ : successor) {
                if (!unreachable.contains(curr, succ)) {
                    queue.add(succ);
                }
            }
        }
        for (Unit unit : cfg) {
            if (!visited.contains(unit)) {
                unit.addTag(new ConfigTag("control-flow unreachable code"));
                unreachable_edges.add(unit);
            }
        }


        return unreachable_edges;
    }

    /**
     * DeadAssign Analysis
     */
    private Set<Unit> findDeadAssign(Body body) {
        BriefUnitGraph cfg = new BriefUnitGraph(body);
        LiveVariables lva = new LiveVariables(cfg);
        lva.doAnalysis();
        boolean is_casestmt = false;
        boolean counter = false;
        int count = 0;
        Set<Unit> deadAssignments = new HashSet<>();
        for (Unit unit : cfg) {
            count++;
            if (unit instanceof TableSwitchStmt) {
                is_casestmt = true;
            } else if (unit instanceof AssignStmt) {
                int last_ops = cfg.getBody().getUnits().size() - 1;
                if (count < last_ops) {
                    Object[] unit_list = cfg.getBody().getUnits().toArray();
                    Arrays.copyOfRange(unit_list, count, last_ops);
                    for (Object tmp : unit_list) {
                        Unit tmp_unit = (Unit) tmp;
                        if (tmp_unit instanceof AssignStmt) {
                            Value v = ((AssignStmt) tmp_unit).getLeftOp();
                            Value v1 = ((AssignStmt) tmp_unit).getRightOp();
                            counter = false;
                            if (v instanceof Local) {
                                if (((Local) v).getName().contains("$") && is_casestmt) {
                                    counter = counter || true;
                                }
                            }
                        }
                    }
                }
                is_casestmt = counter;
                Value v = ((AssignStmt) unit).getLeftOp();
                Value v1 = ((AssignStmt) unit).getRightOp();
                if (v1 instanceof Local) {
                    if (((Local) v1).getName() == ((Local) v).getName()) {
                        is_casestmt = false;
                    }
                }
                AssignStmt assign = (AssignStmt) unit;
                if (v instanceof Local) {
                    Local local = (Local) v;
                    FlowSet<Local> liveSet = lva.getFlowAfter(assign);
                    /** Side Effect */
                    if (!liveSet.contains(local) && !assign.containsInvokeExpr() && !is_casestmt) {
                        deadAssignments.add(unit);
                    }
                }
            }
        }
        return deadAssignments;
    }
}
