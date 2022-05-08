package IntraConstantPropagation;

import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import soot.Local;
import soot.Value;
import soot.jimple.*;

import java.util.*;

public class ConstantPropagationMap {
    /**
     * The map of local to constant value.
     */
    Map<Local, ConstantPropagationValue> map;

    public ConstantPropagationMap() {
        map = new HashMap<Local, ConstantPropagationValue>();
    }

    public ConstantPropagationMap(Map<Local, ConstantPropagationValue> map) {
        this.map = map;
    }

    public ConstantPropagationValue get(Local local) {
        return map.computeIfAbsent(local, l -> ConstantPropagationValue.UNDEF);
    }

    public ConstantPropagationValue put(Local loc, ConstantPropagationValue value) {
        return map.put(loc, value);
    }

    /**
     * Get all vars in the map
     */
    public Set<Local> keySet() {
        return map.keySet();
    }

    public boolean copyFrom(ConstantPropagationMap other) {
        map.putAll(other.map);
        return other.map.equals(map);
    }

    public ConstantPropagationValue compute(Value value) {
        if (value instanceof Local) {
            return get((Local) value);
        } else if (value instanceof IntConstant) {
            return new ConstantPropagationValue(((IntConstant) value).value);
        } else if (value instanceof BinopExpr) {
            BinopExpr binop = (BinopExpr) value;
            Value op1 = binop.getOp1();
            ConstantPropagationValue v1 = compute(op1);
            Value op2 = binop.getOp2();
            ConstantPropagationValue v2 = compute(op2);
            if (v1 == ConstantPropagationValue.UNDEF && v2 == ConstantPropagationValue.UNDEF) {
                return ConstantPropagationValue.UNDEF;
            }
            if (v1 == ConstantPropagationValue.UNDEF || v2 == ConstantPropagationValue.UNDEF) {
                return ConstantPropagationValue.NAC;
            }
            if (v1 == ConstantPropagationValue.NAC || v2 == ConstantPropagationValue.NAC) {
                return ConstantPropagationValue.NAC;
            }
            if (binop instanceof AddExpr) {
                return new ConstantPropagationValue(v1.concrete_val + v2.concrete_val);
            } else if (binop instanceof SubExpr) {
                return new ConstantPropagationValue(v1.concrete_val - v2.concrete_val);
            } else if (binop instanceof MulExpr) {
                return new ConstantPropagationValue(v1.concrete_val * v2.concrete_val);
            } else if (binop instanceof DivExpr) {
                return new ConstantPropagationValue(v1.concrete_val / v2.concrete_val);
            } else if (binop instanceof EqExpr) {
                return new ConstantPropagationValue(v1.concrete_val == v2.concrete_val ? 1 : 0);
            } else if (binop instanceof NeExpr) {
                return new ConstantPropagationValue(v1.concrete_val != v2.concrete_val ? 1 : 0);
            } else if (binop instanceof GeExpr) {
                return new ConstantPropagationValue(v1.concrete_val >= v2.concrete_val ? 1 : 0);
            } else if (binop instanceof GtExpr) {
                return new ConstantPropagationValue(v1.concrete_val > v2.concrete_val ? 1 : 0);
            } else if (binop instanceof LeExpr) {
                return new ConstantPropagationValue(v1.concrete_val <= v2.concrete_val ? 1 : 0);
            } else if (binop instanceof LtExpr) {
                return new ConstantPropagationValue(v1.concrete_val < v2.concrete_val ? 1 : 0);
            }
        }
        return ConstantPropagationValue.NAC;
    }

    public static ConstantPropagationMap meet(ConstantPropagationMap a, ConstantPropagationMap b) {
        ConstantPropagationMap result = new ConstantPropagationMap();
        Set<Local> locals = new HashSet<Local>();
        locals.addAll(a.keySet());
        locals.addAll(b.keySet());
        for (Local local : locals) {
            ConstantPropagationValue a_value = a.get(local);
            ConstantPropagationValue b_value = b.get(local);
            ConstantPropagationValue meet_value = ConstantPropagationValue.meet(a_value, b_value);
            result.put(local, meet_value);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConstantPropagationMap flow = (ConstantPropagationMap) obj;
        return Objects.equals(map, flow.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
