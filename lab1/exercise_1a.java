public class exercise_1a {
    public static boolean isPrime(int n) {
        for (int divisor = 2; divisor < n; divisor++) {
            if (n % divisor == 0) {
                return false;
            }
        }
        return true;
    }
    public static void printPrimes(int n) {
        int i;
        for (i = 2; i <= n; i++) {        // ERROR!!!  Condition should be i <= n.
            if (isPrime(i)) {
                System.out.print(" " + i);
            }
        }
        System.out.print(i);
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 5; i ++) {
            int col = 1;
            while (col < i) {
                System.out.print("x");
                col += 1;
            }
            System.out.println("x");
        }
    }
}