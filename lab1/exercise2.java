public class exercise2 {
    /** Returns the maximum value from m. */
    public static int max(int[] m) {
        int res = 0;
        for (int j : m) {
            if (res < j) {
                res = j;
            }
        }
        return res;
    }
    public static void main(String[] args) {
        int[] numbers = new int[]{9, 2, 15, 2, 22, 10, 6};
        System.out.println(max(numbers));
    }
}
