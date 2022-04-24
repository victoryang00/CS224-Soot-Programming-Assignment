package CombinedAnalysis;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/** See if can do combined Unit Graph dynamically see the order */
public class CombinedUnitGraph extends BriefUnitGraph {

    private List<Unit> lockUnitsForThread1 = new ArrayList<Unit>();
    private List<Unit> unlockUnitsForThread1 = new ArrayList<Unit>();
    private List<Unit> lockUnitsForThread2 = new ArrayList<Unit>();
    private List<Unit> unlockUnitsForThread2 = new ArrayList<Unit>();

    public CombinedUnitGraph(Body b) {
        super(b);
        List<Unit> tempList = this.heads;
        heads = new ArrayList<Unit>();
        heads.addAll(tempList);

        tempList = this.tails;
        tails = new ArrayList<Unit>();
        tails.addAll(tempList);
    }


    public void addLockUnlockUnitsForThread1() {
        Iterator unitIt = this.unitChain.iterator();
        while (unitIt.hasNext()) {
            Unit u = (Unit) unitIt.next();
            Stmt s = (Stmt) u;
            Iterator boxIt = s.getUseBoxes().iterator();
            while( boxIt.hasNext() ) {
                final ValueBox box = (ValueBox) boxIt.next();
                Value value = box.getValue();
                if (value instanceof InterfaceInvokeExpr && ((InterfaceInvokeExpr) value).getMethod().getName().equals("lock")) {
                    this.lockUnitsForThread1.add(u);
                }
                if (value instanceof InterfaceInvokeExpr && ((InterfaceInvokeExpr) value).getMethod().getName().equals("unlock")) {
                    this.unlockUnitsForThread1.add(u);
                }
            }
        }
    }

    public void addLockUnlockUnitsForThread2() {
        Iterator unitIt = this.unitChain.iterator();
        while (unitIt.hasNext()) {
            Unit u = (Unit) unitIt.next();
            Stmt s = (Stmt) u;
            Iterator boxIt = s.getUseBoxes().iterator();
            while( boxIt.hasNext() ) {
                final ValueBox box = (ValueBox) boxIt.next();
                Value value = box.getValue();
                if (value instanceof InterfaceInvokeExpr && ((InterfaceInvokeExpr) value).getMethod().getName().equals("lock")) {
                    if (!this.lockUnitsForThread1.contains(u)) {
                        this.lockUnitsForThread2.add(u);
                    }
                }
                if (value instanceof InterfaceInvokeExpr && ((InterfaceInvokeExpr) value).getMethod().getName().equals("unlock")) {
                    if(!this.unlockUnitsForThread1.contains(u))  {
                        this.unlockUnitsForThread2.add(u);
                    }
                }
            }
        }
    }

    public void addPreds(List<Unit> l, UnitGraph g) {
        Iterator lIt = l.iterator();
        while (lIt.hasNext()) {
            Unit lUnit = (Unit) lIt.next();
            if (!unitChain.contains(lUnit)) {
                this.unitChain.add(lUnit);
            }

            List<Unit> preds = g.getPredsOf(lUnit);
            this.unitToPreds.put(lUnit, preds);
            addPreds(preds, g);
        }
    }

    public void addSuccs(List<Unit> l, UnitGraph g) {
        Iterator lIt = l.iterator();
        while (lIt.hasNext()) {
            Unit lUnit = (Unit) lIt.next();
            if (!unitChain.contains(lUnit)) {
                this.unitChain.add(lUnit);
            }

            List<Unit> succs = g.getSuccsOf(lUnit);
            this.unitToSuccs.put(lUnit, succs);
            addSuccs(succs, g);
        }
    }

    public void addUnlockToLockEdges() {
        Iterator lockIt = this.lockUnitsForThread2.iterator();
        Iterator unlockIt = this.unlockUnitsForThread1.iterator();
        while (unlockIt.hasNext()) {
            Unit unlockUnit = (Unit) unlockIt.next();
            List<Unit> tempSuccs = new ArrayList<Unit>();
            tempSuccs.addAll(this.getSuccsOf(unlockUnit));
            while (lockIt.hasNext()) {
                Unit lockUnit = (Unit) lockIt.next();
                tempSuccs.add(lockUnit);
                List<Unit> tempPreds = new ArrayList<Unit>();
                tempPreds.addAll(this.getPredsOf(lockUnit));
                tempPreds.add(unlockUnit);
                this.unitToPreds.put(lockUnit, tempPreds);
            }
            this.unitToSuccs.put(unlockUnit, tempSuccs);
        }

        lockIt = this.lockUnitsForThread1.iterator();
        unlockIt = this.unlockUnitsForThread2.iterator();
        while (unlockIt.hasNext()) {
            Unit unlockUnit = (Unit) unlockIt.next();
            List<Unit> tempSuccs = new ArrayList<Unit>();
            tempSuccs.addAll(this.getSuccsOf(unlockUnit));
            while (lockIt.hasNext()) {
                Unit lockUnit = (Unit) lockIt.next();
                tempSuccs.add(lockUnit);
                List<Unit> tempPreds = new ArrayList<Unit>();
                tempPreds.addAll(this.getPredsOf(lockUnit));
                tempPreds.add(unlockUnit);
                this.unitToPreds.put(lockUnit, tempPreds);
            }
            this.unitToSuccs.put(unlockUnit, tempSuccs);
        }

    }

    public void addGraph(UnitGraph g) {
        List<Unit> gHeads = g.getHeads();
        Iterator headIt = gHeads.iterator();
        while (headIt.hasNext()) {
            Unit h = (Unit) headIt.next();
            if (!unitChain.contains(h)) {
                this.unitChain.add(h);
            }
            this.heads.add(h);
            List<Unit> succs = g.getSuccsOf(h);
            this.unitToSuccs.put(h, succs);
            addSuccs(succs, g);
        }

        List gTails = g.getTails();
        Iterator tailIt = gTails.iterator();
        while (tailIt.hasNext()) {
            Unit t = (Unit) tailIt.next();
            if (!unitChain.contains(t)) {
                this.unitChain.add(t);
            }
            this.tails.add(t);
            List<Unit> preds = g.getPredsOf(t);
            this.unitToPreds.put(t, preds);
            addPreds(preds, g);
        }
    }
}