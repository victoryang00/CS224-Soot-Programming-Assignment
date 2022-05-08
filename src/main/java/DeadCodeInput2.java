@SuppressWarnings("all")
public class DeadCodeInput2 {
    public static void main(String[] args) {
        controlFlowUnreachable();
    }

    static int controlFlowUnreachable() {
        int x = 1;
        if (true)
            return x;
        int z = 42; // control-flow unreachable code
        foo(z); // control-flow unreachable code
        return 0;
    }

    static void foo(int z) {
    }

}
