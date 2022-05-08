class DeadCodeInput {
    public static void main(String[] args) {
        int x = 1;
        int y = x + 2; // dead assignment
        int z = x + 3;
        use(z);
        int a = x; // dead assignment
    }

    public static void use(int n) {
    }
}
