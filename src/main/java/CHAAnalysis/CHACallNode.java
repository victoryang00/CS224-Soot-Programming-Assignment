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

    public CHACallNode(Unit CalledPlace, SootMethod Callee, int CallSet) {
        switch (CallSet) {
            case 0:
            case 7:
                Kind = Interface;
                break;
            case 1:
            case 6:
                Kind = Virtual;
                break;
            case 2:
            case 5:
                Kind = Special;
                break;
            case 3:
            case 4:
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
