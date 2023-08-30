public class exercise1b {
    public static void drawTriangle(int N) {
        for (int i = 1; i <= N; i ++) {
            int col = 1;
            while (col < i) {
                System.out.print("x");
                col += 1;
            }
            System.out.println("x");
        }
    }
    public static void main(String[] args) {
        drawTriangle(10);
    }
}
