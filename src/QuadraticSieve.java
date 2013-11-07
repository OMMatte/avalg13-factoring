import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class QuadraticSieve {
    private static final int FACTOR_BASE_LIMIT_B_CONSTANT_C = 3;

    private double factorBaseLimitB;

    private BigInteger    currentValue;
    private List<Integer> factorBasePrimes;

    public QuadraticSieve(BigInteger currentValue) {
        this.currentValue = currentValue;
        factorBasePrimes = new ArrayList<Integer>();
    }

    /**
     * Step 4
     */
    public void calculateFactorBaseLimitB() {
        //TODO: Maybe use takeRoot() with BigInteger instead, decimal preciseness might not be needed
        double rootVal = root(currentValue);
        double logVal = 2 * Math.log(rootVal);
        double compositeLogVal = logVal * Math.log(logVal);
        double expo = 0.5 * Math.sqrt(compositeLogVal);
        double finalCalcVal = Math.pow(Math.E, expo);
        factorBaseLimitB = FACTOR_BASE_LIMIT_B_CONSTANT_C * finalCalcVal;
    }

    /**
     * TODO: test Algorithm 1 The Sieve of Eratosthenes
     * @param value
     */
    public void calculateFactoreBase(BigInteger value) {
        for (int testPrime : PrimeTable1.TABLE) {
//            if (testPrime == 2) {
//                continue;
//            }
            if (testPrime > factorBaseLimitB) {
                break;
            }
            int residue = value.pow((testPrime - 1) / 2).mod(BigInteger.valueOf(testPrime)).intValue();
            if (residue == 1) {
                factorBasePrimes.add(testPrime);
            }
        }
    }

    /**
     * Checking if a given value is smooth with our factorBase.
     * This means checking if the value is a composite of our primes.
     * @param value
     * @return
     */
    public boolean isSmooth(int value) {
        for (int factor : factorBasePrimes) {
            while (value % factor != 0) {
                value = value / factor;
            }
        }
        if (value == 1) {
            return true;
        } else {
            return false;
        }

    }

    public BigInteger Q(double x) {
        //Using long to floor the value.
        long rootVal = (long)root(currentValue);

        //TODO: I'm guessing this should be integer and not decimal.
        BigInteger quad = BigInteger.valueOf((long) ((rootVal + x) * (rootVal + x)));

        BigInteger result = quad.subtract(currentValue);

        return result;
    }

    public double root(BigInteger value) {
        return PrimeDivider.takeRoot(2, new BigDecimal(currentValue), BigDecimal.ZERO).doubleValue();
    }

    public List<Integer> getFactorBasePrimes() {
        return factorBasePrimes;
    }
}
