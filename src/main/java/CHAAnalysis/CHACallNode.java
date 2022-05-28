package CHAAnalysis;

import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;

public class CHACallNode {
    public String Interface = "invokeinterface";
    public String Virtual = "invokevirtual";
    public String Special = "invokespecial";
    public String Static = "invokestatic";

    public String Kind;
    public Unit CalledPlace;
    public SootMethod Callee;

    public CHACallNode(int CallSet, Unit CalledPlace, SootMethod Callee) {
        switch (CallSet) {
            case 0:
                Kind = Interface;
                break;
            case 1:
                Kind = Virtual;
                break;
            case 2:
                Kind = Special;
                break;
            case 3:
                Kind = Static;
                break;
        }
        this.CalledPlace = CalledPlace;
        this.Callee = Callee;
    }

    @Override
    public String toString() {
        return "@" + CalledPlace.getTag(LineNumberTag.IDENTIFIER) + ": " + CalledPlace.toString() + "->" + Callee.getSignature();
    }
}
