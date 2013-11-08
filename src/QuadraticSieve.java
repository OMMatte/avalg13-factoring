import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class QuadraticSieve {
    private static final int   FACTOR_BASE_LIMIT_B_CONSTANT_C = 3;
    private static final int   SIEVE_SPAN                     = 1000; //TODO: Use log of biggest element in factor base instead
    private static final float SMOOTH_ZERO                    = 0.1f;
    private static final int   SMOOTH_EXTRAS                  = 1;

    private double factorBaseLimitB;

    private List<Integer> factorBasePrimes;

    public QuadraticSieve() {
        factorBasePrimes = new ArrayList<Integer>();
    }

    /**
     * Step 4
     */
    public void calculateFactorBaseLimitB(BigInteger value) {
        double rootVal = PrimeDivider.root(2, value, true).doubleValue(); //TODO: Maybe use takeRoot() with BigInteger instead, decimal preciseness might not be needed
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
            //            if (testPrime == 2) {
            //                continue;
            //            }
            int residue = legendre(value, testPrime);
            if (residue == 1) {
                factorBasePrimes.add(testPrime);
            }
        }
    }

    int legendre(BigInteger N, int p) {
        return N.modPow(BigInteger.valueOf((p - 1) / 2), BigInteger.valueOf(p)).intValue();
    }

    /**
     * Checking if a given value is smooth with our factorBase.
     * This means checking if the value is a composite of our primes.
     *
     * @param value
     * @return
     */
    //    public boolean isSmooth(int value) {
    //        for (int factor : factorBasePrimes) {
    //            while (value % factor != 0) {
    //                value = value / factor;
    //            }
    //        }
    //        if (value == 1) {
    //            return true;
    //        } else {
    //            return false;
    //        }
    //    }
    public boolean isSmooth(float value) {
        if (value > -SMOOTH_ZERO && value < SMOOTH_ZERO) {
            return true;
        }
        return false;
    }

    public BigInteger Q(double x, BigInteger N) {
        //Using long to floor the value.
        long rootVal = root(N).longValue();

        //TODO: I'm guessing this should be integer and not decimal.
        BigInteger quad = BigInteger.valueOf((long) ((rootVal + x) * (rootVal + x)));
        BigInteger result = quad.subtract(N);

        return result;
    }

    public BigInteger root(BigInteger value) {
        return PrimeDivider.root(2, value, true);
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

    public int[] tonelliShanks(BigInteger N, int prime) { //TODO: return 2 values if needed
        //Factor out 2,s frome prime
        int Q = prime - 1;
        int S = 0;
        while (Q % 2 == 0) {
            S++;
            Q = Q / 2;
        }
        int R;
        if (S == 1) {
            R = N.modPow(BigInteger.valueOf((prime + 1) / 4), BigInteger.valueOf(prime)).intValue();
        } else {

            int residue = 0;
            int Z;
            for (Z = 2; residue != prime - 1; Z++) {
                residue = (int) ((Math.pow(Z, (prime - 1) / 2)) % (long)prime);
            }
            //Z gets plussed one to many times;
            Z--;

            int C = ((int) Math.pow(Z, Q)) % prime;

            R = N.modPow(BigInteger.valueOf((Q + 1) / 2), BigInteger.valueOf(prime)).intValue();

            int t = N.modPow(BigInteger.valueOf(Q), BigInteger.valueOf(prime)).intValue();

            int M = S;

            while (t != 1) {
                int tempVal = 1;
                int i = 1;
                while (i < M) {
                    tempVal *= 2;
                    if (((int) Math.pow(t, tempVal)) % prime == 1) {
                        break;
                    }
                    i++;
                }


                int b = (int) Math.pow(C, Math.pow(2, M - i - 1));
                R = (R * b) % prime;
                t = (t * b * b) % prime;
                C = (b * b) % prime;
                M = i;
            }
        }

        int[] result = new int[]{R, prime -R};
        for(int i = 0; i < result.length; i++){
            result[i] = (int) ((((long)result[i]) - root(N).longValue()) % prime);
            if(result[i] < 0){
                result[i] += prime;
            }
        }

        return result;
    }

    private void initQValues(ArrayList<Float> qValues, int size, BigInteger N) {
        qValues.ensureCapacity(size);

        for (int x = qValues.size(); x < size; x++) {
            qValues.add(x, (float) Math.log(Q(x, N).intValue()));
        }
    }

    public ArrayList<Integer> sieve(BigInteger N) {
        ArrayList<Integer> smoothX = new ArrayList<Integer>(factorBasePrimes.size() + SMOOTH_EXTRAS);
        sieve(smoothX, new ArrayList<Float>(SIEVE_SPAN), N, 0);
        return smoothX;
    }

    void sieve(ArrayList<Integer> smoothX, ArrayList<Float> qValues, BigInteger N, int lowX) {
        initQValues(qValues, SIEVE_SPAN, N);

        for (int prime : factorBasePrimes) {
            //TODO: Precalculate this shit (tonelliShanks
            //            while (true) {
            float logP = (float) Math.log(prime);
            int[] xArray;
            if (prime == 2) {
                xArray = bruteForceXValues(lowX, prime, N); //TODO check if we can skip this
            } else {
                xArray = tonelliShanks(N, prime);
            }
            for (int x : xArray) {
                if (x >= qValues.size() || x < lowX) {
                    break;
                }
                do {
                    qValues.set(x, qValues.get(x) - logP);
                    if (isSmooth(qValues.get(x))) {
                        smoothX.add(x);
                        if (smoothX.size() >= factorBasePrimes.size() + SMOOTH_EXTRAS) {
                            return;
                        }
                    }
                    x += prime;
                }
                while (x < qValues.size());
            }

            //                prime *= prime;
            //            }
        }

        sieve(smoothX, qValues, N, qValues.size());
    }

    private int[] bruteForceXValues(int x, int prime, BigInteger N) {
        BigInteger Q = Q(x, N);
        if (Q.mod(BigInteger.valueOf(prime)).equals(BigInteger.ZERO)) {
            return new int[]{ x };
        } else {
            return new int[]{ x + 1 };
        }
    }

    byte[][] buildMatrix(ArrayList<Integer> smoothX, BigInteger N) {
        byte[][] matrix = new byte[smoothX.size()][factorBasePrimes.size()];
        for (int row = 0; row < matrix.length; row++) {
            BigInteger qValue = Q(smoothX.get(row), N);
            for (int col = 0; col < matrix[0].length; col++) {
                boolean odd = false;
                BigInteger prime = BigInteger.valueOf(factorBasePrimes.get(col));
                BigInteger[] qr = qValue.divideAndRemainder(prime);
                if (qr[1].equals(BigInteger.ZERO)) {
                    qValue = qr[0];
                    odd = !odd;
                }
                matrix[row][col] = (byte) (odd ? 1 : 0);
            }
        }
        return matrix;
    }

    void gaussElimination(byte[][] matrix, boolean[] markedRows) {
        for (int j = 0; j < matrix[0].length; j++) {
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i][j] == 1) {
                    markedRows[i] = true;
                    for (int k = 0; k < matrix[0].length; k++) {
                        if (k == j) {
                            continue;
                        }
                        if (matrix[i][k] == 1) {
                            for (int ii = 0; ii < matrix.length; ii++) {
                                matrix[ii][k] ^= matrix[ii][j];
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    boolean[] findLeftNullSpaceVector(byte[][] matrix, boolean[] markedPos) {
        boolean[] selectedRows = new boolean[matrix.length];
        int marked = 0;
        for (int i = 0; i < markedPos.length; i++) {
            if (!markedPos[i]) {
                marked = i;
                break;
            }
        }
        for (int col = 0; col < matrix[0].length; col++) {
            if (matrix[marked][col] == 1) {
                for (int row = 0; row < matrix.length; row++) {
                    if (matrix[row][col] == 1) {
                        selectedRows[row] = true;
                    }
                }
            }
        }

        return selectedRows;
    }

    BigInteger finalizeGCD(BigInteger N, boolean[] selectedRows, ArrayList<Integer> smoothX) {
        BigInteger qValue = BigInteger.ONE;
        BigInteger xValue = BigInteger.ONE;
        BigInteger rootN = root(N);
        for (int row = 0; row < selectedRows.length; row++) {
            if (selectedRows[row]) {

                qValue = qValue.multiply(Q(smoothX.get(row), N));
                xValue = xValue.multiply((rootN.add(BigInteger.valueOf(smoothX.get(row)))));
            }
        }
        qValue = PrimeDivider.root(2, qValue, true);
        BigInteger fuckingFinalResult = N.gcd(xValue.subtract(qValue));
        return fuckingFinalResult;
    }
}
