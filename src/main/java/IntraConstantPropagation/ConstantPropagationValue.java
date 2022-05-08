package IntraConstantPropagation;

import java.util.Objects;

public class ConstantPropagationValue {
    public final static ConstantPropagationValue UNDEF = new ConstantPropagationValue(99999999);
    public final static ConstantPropagationValue NAC = new ConstantPropagationValue(-99999999);
    public int concrete_val;

    public ConstantPropagationValue() {
    }

    public ConstantPropagationValue(int val) {
        this.concrete_val = val;
    }

    public static ConstantPropagationValue meet(ConstantPropagationValue v1, ConstantPropagationValue v2) {
        if (v1==UNDEF) {
            return v2;
        }
        if (v2==UNDEF) {
            return v1;
        }
        if (v1==NAC || v2==NAC) {
            return NAC;
        }
        if (v1.concrete_val == v2.concrete_val){
            return new ConstantPropagationValue(v1.concrete_val);
        }
        return NAC;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConstantPropagationValue cpValue = (ConstantPropagationValue) obj;
        return concrete_val == cpValue.concrete_val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(concrete_val);
    }

    @Override
    public String toString() {
        if (this == NAC) {
            return "NAC";
        }
        if (this == UNDEF) {
            return "UNDEF";
        }
        return String.valueOf(concrete_val);
    }
}
