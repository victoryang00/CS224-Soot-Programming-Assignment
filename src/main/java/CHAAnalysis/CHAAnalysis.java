package CHAAnalysis;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import java.util.*;

enum InvokeMorphism {
    InterfaceInvokeExpr,
    VirtualInvokeExpr,
    SpecialInvokeExpr,
    StaticInvokeExpr
}

/**
 * Main class for CHAAnalysis
 */
public class CHAAnalysis {
    public static CHAAnalysis analysis = new CHAAnalysis();

    public Hierarchy cha_class = new Hierarchy();

    public Collection<SootMethod> entries;

    public ReachableMethods reachableSets = new ReachableMethods();

    public Map<SootMethod, Set<CHACallNode>> calleeCallerMap = new HashMap<SootMethod, Set<CHACallNode>>();
    public Map<Unit, SootMethod> unitSootMethodMap = new HashMap<Unit, SootMethod>();

    CHAAnalysis() {
    }

    public void resolve(Chain<SootClass> classes_) {
        for (SootClass class_ : classes_) {
            for (SootMethod method_ : class_.getMethods()) {
                if (method_.isAbstract())
                    continue;
                Body body = method_.retrieveActiveBody();
                if (body != null) {
                    for (Unit unit : body.getUnits())
                        unitSootMethodMap.put(unit, method_);
                }
            }
        }
        Collection<SootMethod> init_entry;
        if (entries != null) {
            init_entry= entries;
        }
        entries = new LinkedList<>();
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            for (SootMethod method : clazz.getMethods()) {
                if ("main".equals(method.getName())) {
                    entries.add(method);
                }
            }
        }
        reachableSets.addAll(entries);

    }

    /**
     * To match a sootMethod in sootClass
     */
    public SootMethod dispatch(SootClass clazz_, SootMethod method_) {
        for (SootMethod method : clazz_.getMethods()) {
            if (method.isConcrete()) {
                if (method.getName() == method_.getName()) {
                    if (method.getParameterCount() == method_.getParameterCount()) {
                        int count = 0;
                        boolean matched = true;
                        for (Type type : method.getParameterTypes()) {
                            Type typetoMatch = method_.getParameterType(count);
                            if (type.toQuotedString() != typetoMatch.toQuotedString()) {
                                matched = false;
                            }
                            count++;
                        }
                        if (matched) {
                            return method;
                        }
                    }
                }
            }
        }
        SootClass super_ = clazz_.getSuperclass();
        if (super_ == null) {
            return null;
        } else {
            return dispatch(super_, method_);
        }
    }

    public ReachableMethods resolveCallee(Unit unit) {
        Stmt stmt = (Stmt) unit;
        InvokeExpr invoke = stmt.getInvokeExpr();
        int toPass = InvokeMorphism.valueOf(invoke.getClass().getSimpleName()).ordinal();
        SootMethod method = invoke.getMethod();
        SootClass class_ = method.getDeclaringClass();
        switch (toPass) {
            case 3:
            return new ReachableMethods(Collections.singleton(invoke.getMethod()));
            case 2: {
                SootMethod dis = dispatch(class_,method);
                if (dis!=null) {
                    return new ReachableMethods(Collections.singleton(dis));
                }else{
                    return new ReachableMethods();
                }
            }
        }
        List<SootClass> classes;
        /** Check wether is class */
        if (class_.isInterface()) {
            classes = cha_class.getSubinterfacesOfIncluding(class_);
        } else {
            classes = cha_class.getSubclassesOfIncluding(class_);
        }
        Set<SootMethod> result = new HashSet<>();
        for (SootClass inside_class : classes) {
            SootMethod dispatch = dispatch(inside_class, method);
            if (dispatch != null) {
                result.add(dispatch);
            }
        }
        return new ReachableMethods(result);
    }
}
