public class CHAInput{
    public static void main(String[] args) {
        int a = 6;
        int tmp = addOne(a);
        int b = tmp;
        int tmp1 = ten();
        b = tmp1;
        return;
    }

    public static int addOne(int y){
        int con = 1;
        y = y + con;
        return y;
    }
    public static int ten(){
        int con = 10;
        return con;
    }
}

