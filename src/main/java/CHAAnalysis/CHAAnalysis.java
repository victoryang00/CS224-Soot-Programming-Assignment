package CHAAnalysis;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import java.util.*;

enum InvokeMorphism {
    InterfaceInvokeExpr,
    VirtualInvokeExpr,
    SpecialInvokeExpr,
    StaticInvokeExpr,
    JStaticInvokeExpr,
    JSpecialInvokeExpr,
    JVirtualInvokeExpr,
    JInterfaceInvokeExpr
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
        Collection<SootMethod> init_entry;

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
        if (entries == null) {
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
        init_entry = entries;
        Queue<SootMethod> queue = new LinkedList<>(init_entry);
        while (!queue.isEmpty()) {
            SootMethod method = queue.poll();
            if (method.hasActiveBody()) {
                List<Unit> called = new LinkedList<>();
                if (method.hasActiveBody()) {
                    Body body = method.getActiveBody();
                    for (Unit unit : body.getUnits()) {
                        Stmt stmt = (Stmt) unit;
                        if (stmt.containsInvokeExpr()) {
                            called.add(stmt);
                        }
                    }
                    for (Unit call : called) {
                        ReachableMethods CalleeMap = resolveCallee(call);
                        for (SootMethod callee : CalleeMap) {
                            if (!reachableSets.contains(callee)) {
                                queue.add(callee);
                            }
                            reachableSets.add(callee);
                            /** Add one Edge */

                            /** Create the CHACallNode*/
                            InvokeExpr invoke = ((Stmt)call).getInvokeExpr();
                            CHACallNode callNode = new CHACallNode(call, callee, InvokeMorphism.valueOf(invoke.getClass().getSimpleName()).ordinal());
                            Set<CHACallNode> callers = calleeCallerMap.computeIfAbsent(callee, k -> new HashSet<>());
                            callers.add(callNode);
                            /** check if whether in the unit soot method */
                            SootMethod caller = unitSootMethodMap.get(call);
                            Set<CHACallNode> callees = calleeCallerMap.computeIfAbsent(caller, k -> new HashSet<>());
                            callees.add(callNode);
                        }
                    }
                }
            }
        }
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
        List<SootClass> classes;

        InvokeExpr invoke = stmt.getInvokeExpr();
        int toPass = InvokeMorphism.valueOf(invoke.getClass().getSimpleName()).ordinal();
        SootMethod method = invoke.getMethod();
        SootClass class_ = method.getDeclaringClass();
        switch (toPass) {
            case 3:
                return new ReachableMethods(Collections.singleton(invoke.getMethod()));
            case 2: {
                SootMethod dis = dispatch(class_, method);
                if (dis != null) {
                    return new ReachableMethods(Collections.singleton(dis));
                } else {
                    return new ReachableMethods();
                }
            }
        }
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
    public Set<CHACallNode> getCallout(SootMethod method){
        Set<CHACallNode> result = calleeCallerMap.computeIfAbsent(method, k -> new HashSet<>());
        return Collections.unmodifiableSet(result);
    }
}
