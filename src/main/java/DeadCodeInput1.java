public class DeadCodeInput1 {
    public static void main(String[] args){
        branch();
    }
    static int branch() {
        int x = 10;
        int y = 1;
        int z;
        if (x > y) {
            z = 100;
        } else {
            z = 200; // unreachable branch
        }
        return z;
    }
}
