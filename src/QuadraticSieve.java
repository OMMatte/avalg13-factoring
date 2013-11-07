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


    private List<Integer> factorBasePrimes;

    public QuadraticSieve() {
        factorBasePrimes = new ArrayList<Integer>();
    }

    /**
     * Step 4
     */
    public void calculateFactorBaseLimitB(BigInteger value) {
        double rootVal = PrimeDivider.root(2, value, BigInteger.ZERO).doubleValue(); //TODO: Maybe use takeRoot() with BigInteger instead, decimal preciseness might not be needed
        double logVal = 2 * Math.log(rootVal);
        double compositeLogVal = logVal * Math.log(logVal);
        double expo = 0.5 * Math.sqrt(compositeLogVal);
        double finalCalcVal = Math.pow(Math.E, expo);
        factorBaseLimitB = FACTOR_BASE_LIMIT_B_CONSTANT_C * finalCalcVal;
    }

    /**
     * TODO: test Algorithm 1 The Sieve of Eratosthenes
     *
     * @param value
     */
    public void calculateFactoreBase(BigInteger value) {
        //        factorBasePrimes.add(-1);
        for (int testPrime : PrimeTable1.TABLE) {
//            if (testPrime == 2) {
//                continue;
//            }
            if (testPrime > factorBaseLimitB) {
                break;
            }
            int residue = value.modPow(BigInteger.valueOf((testPrime - 1) / 2), BigInteger.valueOf(testPrime)).intValue();
            if (residue == 1) {
                factorBasePrimes.add(testPrime);
            }
        }
    }

    /**
     * Checking if a given value is smooth with our factorBase.
     * This means checking if the value is a composite of our primes.
     *
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

    public BigInteger Q(double x, BigInteger value) {
        //Using long to floor the value.
        long rootVal = (long)root(value);

        //TODO: I'm guessing this should be integer and not decimal.
        BigInteger quad = BigInteger.valueOf((long) ((rootVal + x) * (rootVal + x)));

        BigInteger result = quad.subtract(value);

        return result;
    }

    public double root(BigInteger value) {
        return PrimeDivider.takeRoot(2, new BigDecimal(value), BigDecimal.ZERO).doubleValue();
    }

    public List<Integer> getFactorBasePrimes() {
        return factorBasePrimes;
    }

    public BigInteger factorOut(BigInteger value, BigInteger prime) {
        while (value.mod(prime).compareTo(BigInteger.ZERO) == 0) {
            value = value.divide(prime);
        }
        return value;
    }

    public int factorOut(int value, int prime) {
        while (value % prime == 0) {
            value = value / prime;
        }
        return value;
    }

    public int findSatisfyingS(BigInteger value, int prime) {
        //Factor out 2,s frome prime
        int Q = prime - 1;
        int S = 0;
        while (Q % 2 == 0) {
            S++;
            Q = Q / 2;
        }
        if (S == 1) {
            return value.modPow(BigInteger.valueOf((prime + 1) / 4), BigInteger.valueOf(prime)).intValue();
        }

        int residue = 0;
        int Z;
        for (Z = 2; residue != -1; Z++) {
            residue = ((int) Math.pow(Z, (prime - 1) / 2)) % prime;
        }

        int C = ((int) Math.pow(Z, Q)) % prime;

        int R = value.modPow(BigInteger.valueOf((Q + 1) / 2), BigInteger.valueOf(prime)).intValue();

        int t = value.modPow(BigInteger.valueOf(Q), BigInteger.valueOf(prime)).intValue();

        int M = S;

        while (t != 1) {
            int tempVal = 1;
            int i;
            for (i = 1; i < M; i++) {
                tempVal *= 2;
                if (((int) Math.pow(t, tempVal)) % prime != 1) {
                    break;
                }
            }

            int b = (int) Math.pow(C, Math.pow(2, M - i - 1));
            R = R * b % prime;
            t = t*b*b % prime;
            C = b*b % prime;
            M = i;
        }

        return t;
    }
}
