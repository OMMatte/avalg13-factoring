import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class PrimeDivider {
    public static final int IS_PRIME_CERTAINTY      = 7;
    public static final int PERFECT_POTENS_MAX_ROOT = 6;

    private BigInteger       currentValue;
    private List<BigInteger> foundPrimes;

    private int amountPerfectPotenses;


    public PrimeDivider() {
        foundPrimes = new ArrayList<BigInteger>();
    }

    public void init(BigInteger bi) {
        currentValue = bi;
        foundPrimes.clear();
    }

    public boolean solve() {
        if (currentValue.equals(BigInteger.ONE) || currentValue.isProbablePrime(IS_PRIME_CERTAINTY)) {
            foundPrimes.add(currentValue);
            return true;
        }
        boolean done = false;
        done = trialDivision();
        if (done) {
            return true;
        }

        done = perfectPotens(); //TODO: Might be totally useless, no more hits in Kattis, check the exception below
        if (done) {
            return true;
        }
        QuadraticSieve qs = new QuadraticSieve(currentValue);
        return false;
    }



    public List<BigInteger> getFoundPrimes() {
        return foundPrimes;
    }

    private boolean trialDivision() {
        trialDivision(PrimeTable1.TABLE, currentValue);
        trialDivision(PrimeTable2.TABLE, currentValue);
        //        trialDivision(PrimeTable3.TABLE, currentValue);
        //        trialDivision(PrimeTable4.TABLE, currentValue);
        if (currentValue.equals(BigInteger.ONE)) {
            return true;
        } else if (currentValue.isProbablePrime(IS_PRIME_CERTAINTY)) {
            foundPrimes.add(currentValue);
            return true;
        }
        return false;
    }

    public void trialDivision(int[] table, BigInteger n) {
        BigInteger p;
        BigInteger[] quotientAndRemainder;
        for (int lp : table) {
            p = BigInteger.valueOf(lp);
            if (n.bitCount() < 40 && p.multiply(p).compareTo(n) == 1) {
                break;
            }
            while (true) {
                quotientAndRemainder = n.divideAndRemainder(p);
                if (quotientAndRemainder[1].intValue() == 0) {
                    n = quotientAndRemainder[0];
                    foundPrimes.add(p);
                } else {
                    break;
                }
            }
        }
        currentValue = n;
    }

    boolean perfectPotens() {

        amountPerfectPotenses = 1;
        boolean beginNextDepth = true;
        while (beginNextDepth) {
            beginNextDepth = false;
            BigInteger startX = BigInteger.ONE; //TODO: Better start guess
            for (int root = 2; root <= PERFECT_POTENS_MAX_ROOT; root++) {
                BigInteger newNumber = takeRoot(root, currentValue, startX);
                if (newNumber.compareTo(BigInteger.ZERO) != 0) {
                    amountPerfectPotenses *= root;
                    currentValue = newNumber;
                    beginNextDepth = true;
                    break;
                }
            }
            if (currentValue.isProbablePrime(IS_PRIME_CERTAINTY)) {
                for (int i = 0; i < amountPerfectPotenses; i++) {
                    foundPrimes.add(currentValue);
                }
                return true;
            }
        }
        return false;
    }

    public BigInteger takeRoot(int root, final BigInteger n, BigInteger x) {

        // Specify the starting value in the search for the cube root.
        BigInteger prevX = null;

        BigInteger rootBD = BigInteger.valueOf(root);
        // Search for the cube root via the Newton-Raphson loop. Output each successive iteration's value.
        while (true) {
            x = x.subtract(x.pow(root)
                            .subtract(n)
                            .divide(rootBD.multiply(x.pow(root - 1))));
            if (prevX != null && prevX.subtract(x).abs() == BigInteger.ZERO) { break; }
            prevX = x;
        }

        BigInteger testN = x.pow(root);
        int sign = testN.compareTo(n);
        int startSign = sign;

        while (sign == startSign && sign != 0) {
            x = x.subtract(BigInteger.valueOf(startSign));
            testN = x.pow(root);
            sign = testN.compareTo(n);
        }
        if (sign != 0) {
            return BigInteger.ZERO;
        }
        return x;
    }


    public static BigDecimal takeRoot(int root, BigDecimal n, BigDecimal maxError) {
        int MAXITER = 5000;

        // Specify a math context with 40 digits of precision.
        MathContext mc = new MathContext(40);

        // Specify the starting value in the search for the cube root.
        BigDecimal x;
        x = new BigDecimal("1", mc);


        BigDecimal prevX = null;

        BigDecimal rootBD = new BigDecimal(root, mc);
        // Search for the cube root via the Newton-Raphson loop. Output each successive iteration's value.
        for (int i = 0; i < MAXITER; ++i) {
            x = x.subtract(x.pow(root, mc)
                            .subtract(n, mc)
                            .divide(rootBD.multiply(x.pow(root - 1, mc), mc), mc), mc);
            if (prevX != null && prevX.subtract(x).abs().compareTo(maxError) < 0) { break; }
            prevX = x;
        }

        return x;
    }
}