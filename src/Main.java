import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class Main {

    public static boolean debug = false;

    public static void main(String args[]) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        List<BigInteger> inputNumbers = new ArrayList<BigInteger>();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.contains(";")) {
                break;
            }
            inputNumbers.add(new BigInteger(line));
        }

        PrimeDivider primeDivider = new PrimeDivider();

        for (BigInteger n : inputNumbers) {
            primeDivider.init(n);
            if (primeDivider.solve()) {
                List<BigInteger> primes = primeDivider.getFoundPrimes();
                for (BigInteger prime : primes) {
                    System.out.println(prime);
                }
            } else {
                System.out.println("fail");
            }
            System.out.println();
        }
    }
}
