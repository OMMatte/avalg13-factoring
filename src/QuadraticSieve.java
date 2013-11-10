import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class QuadraticSieve {
    static final int        FACTOR_BASE_LIMIT_B_CONSTANT_C = 2;
    static final float      SMOOTH_ZERO                    = (float) (0.1f);
    static final int        SMOOTH_EXTRAS                  = 1;
    static final BigInteger TWO                            = BigInteger.valueOf(2);
    int SIEVE_SPAN;

    private double factorBaseLimitB;

    private List<Integer> factorBasePrimes;

    int[]      sieveCurrentX;
    int[]      sievePrimeOffset;
    float[]    sievePrimeLog;
    BigInteger rootValForN;
    //    ArrayList<BigInteger> originalQValues;
    float[]    primeLogValues;

    byte[][] matrix;


    BigInteger qValueForLastX;
    BigInteger lastValueForXoffSet;
    BigInteger sqrRootTimes2;
    float      threshHold;

    long timeLimit;

    public QuadraticSieve(long timeLimit) {
        this.timeLimit = System.currentTimeMillis() + timeLimit;
    }

    public QuadraticSieve(BigInteger N, long timeLimit) {
        init(N);
        SIEVE_SPAN = (int) Math.pow(N.bitLength() / 10, 5);
        this.timeLimit = System.currentTimeMillis() + timeLimit;
    }

    public void init(BigInteger N) {
        factorBasePrimes = new ArrayList<Integer>();
        //        originalQValues = new ArrayList<BigInteger>();

        rootValForN = PrimeDivider.root(2, N, true);
        sqrRootTimes2 = rootValForN.multiply(BigInteger.valueOf(2));
        qValueForLastX = rootValForN.multiply(rootValForN).subtract(N);
        lastValueForXoffSet = BigInteger.ZERO;
    }

    /**
     * Step 4
     */
    public void calculateFactorBaseLimitB(BigInteger N) {
        double logVal = (2 * Math.log(rootValForN.longValue()));
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
            //TODO add 2 manually
            if (testPrime == 2) {
                factorBasePrimes.add(testPrime);
            } else {
                int residue = legendre(value, testPrime);
                if (residue == 1) {
                    factorBasePrimes.add(testPrime);
                }
            }
        }
        threshHold = (float) Math.log(factorBasePrimes.get(factorBasePrimes.size() - 1));
        //TODO test:  (int) Math.log(factorBasePrimes.get(getFactorBasePrimes().size()-1));
    }

    int legendre(BigInteger N, int p) {
        int result = 1;
        int a = N.mod(BigInteger.valueOf(p)).intValue();
        int power = (p - 1) / 2;

        while (power > 0) {
            if (power % 2 == 1) {
                result = (result * a) % p;
            }
            a = (a * a) % p;
            power = power / 2;
        }
        if (result - p == -1) {
            result = result - p;
        }
        return result;
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
        if (value < SMOOTH_ZERO) {
            return true;
        }
        return false;
    }

    public BigInteger Q(BigInteger lastValue, BigInteger offset, BigInteger sqrTimes2) {
        BigInteger result = lastValue.add(sqrTimes2).add(offset);
        return result;
    }

    public BigInteger Q(BigInteger x, BigInteger N) {
        BigInteger rootVal = rootValForN;
        BigInteger quad = ((rootVal.add(x)).multiply(rootVal.add(x)));
        return quad.subtract(N);
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

    /**
     * Since all values are legendre 1 we do not need to test legendre for the input N and prime.
     *
     * @param N
     * @param prime
     * @return
     */
    public int[] tonelliShanks(BigInteger N, int prime) { //TODO: return 2 values if needed
        //Factor out 2,s frome prime
        int Q = prime - 1;
        int S = 0;
        while (Q % 2 == 0) {
            S++;
            Q = Q / 2;
        }
        BigInteger R;
        if (S == 1) {
            R = N.modPow(BigInteger.valueOf((prime + 1) / 4), BigInteger.valueOf(prime));
        } else {

            int residue = 0;
            int Z;
            for (Z = 2; ; Z++) {
                residue = legendre(BigInteger.valueOf(Z), prime);
                if (residue == -1) {
                    break;
                }
            }

            BigInteger C = BigInteger.valueOf(Z).modPow(BigInteger.valueOf(Q), BigInteger.valueOf(prime));

            R = N.modPow(BigInteger.valueOf((Q + 1) / 2), BigInteger.valueOf(prime));

            BigInteger t = N.modPow(BigInteger.valueOf(Q), BigInteger.valueOf(prime));

            int M = S;

            while (!t.equals(BigInteger.ONE)) {
                int tempVal = 1;
                int i = 1;
                while (i < M) {
                    tempVal *= 2; //TODO: Check so it does not go to high
                    if (tempVal > Math.pow(2, 20)) {
                        System.out.println(i + " " + tempVal);
                        throw new RuntimeException();
                    }
                    if (t.modPow(BigInteger.valueOf(tempVal), BigInteger.valueOf(prime)).equals(BigInteger.ONE)) {
                        break;
                    }
                    i++;
                }
                if (i == M && i != 1) {
                    i--;
                }
                if (M - i > 25) {
                    throw new RuntimeException();
                }


                BigInteger b = C.modPow(BigInteger.valueOf((long) Math.pow(2, M - i - 1)), BigInteger.valueOf(prime));
                R = R.multiply(b).mod(BigInteger.valueOf(prime));
                t = t.multiply(b).multiply(b).mod(BigInteger.valueOf(prime));
                C = b.multiply(b).mod(BigInteger.valueOf(prime));
                M = i;
            }
        }

        int[] result = new int[]{ R.intValue(), prime - R.intValue() };
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) ((((long) result[i]) - rootValForN.longValue()) % prime);
            if (result[i] < 0) {
                result[i] += prime;
            }
        }

        return result;
    }

    //To be used in the matrix.
    private byte[] row;

    public ArrayList<Long> preSieve(BigInteger N) {
        int sieveSize;
        if (factorBasePrimes.contains(2)) {
            sieveSize = factorBasePrimes.size() * 2 - 1;
        } else {
            sieveSize = factorBasePrimes.size() * 2;
        }
        sievePrimeLog = new float[sieveSize];
        sievePrimeOffset = new int[sieveSize];
        sieveCurrentX = new int[sieveSize];
        primeLogValues = new float[sieveSize];

        //Special for prime 2!

        int pos = 0;
        for (int i = 0; i < factorBasePrimes.size(); i++) {
            int prime = factorBasePrimes.get(i);
            if (prime == 2) {
                sievePrimeOffset[pos] = prime;
                sievePrimeLog[pos] = (float) Math.log(prime);
                sieveCurrentX[pos] = bruteForceXValuesFor2(N);
                primeLogValues[pos] = (float) Math.log(prime);

                pos++;
            } else {
                int[] xArray = tonelliShanks(N, prime);
                float logPrime = (float) Math.log(prime);
                for (int x : xArray) {
                    sievePrimeOffset[pos] = prime;
                    sievePrimeLog[pos] = logPrime;
                    sieveCurrentX[pos] = x;
                    primeLogValues[pos] = (float) Math.log(prime);
                    pos++;
                }
            }
        }


        ArrayList<Long> smoothX = new ArrayList<Long>(factorBasePrimes.size() + SMOOTH_EXTRAS);
        matrix = new byte[factorBasePrimes.size() + SMOOTH_EXTRAS][factorBasePrimes.size()];

        float[] qValues = new float[SIEVE_SPAN];
        //        originalQValues = new ArrayList<BigInteger>(sieveSpan);

        //Special to avoid calcualation problems in Q(), ask Mathias
        //        qValues.add((float) Math.log(qValueForLastX.doubleValue()));
        //        originalQValues.add(qValueForLastX);
        //        lastValueForXoffSet = lastValueForXoffSet.add(BigInteger.ONE);

        row = new byte[factorBasePrimes.size()];

        initQValues(0, qValues, N);
        if (sieve(0, smoothX, qValues, N)) {
            return smoothX;
        } else {
            return null;
        }
    }

    private void initQValues(long offsetX, float[] qLogValues, BigInteger N) {
        float qValueForLastX = (float) Math.log(Q(BigInteger.valueOf(offsetX + qLogValues.length - 1), N).doubleValue());
        for (int x = 0; x < qLogValues.length; x++) {
            qLogValues[x] = qValueForLastX;
            //            qValueForLastX = Q(qValueForLastX, lastValueForXoffSet, sqrRootTimes2);
            //            qValues.add((float) Math.log(qValueForLastX.doubleValue()));
            //            originalQValues.add(qValueForLastX);
            //            lastValueForXoffSet = lastValueForXoffSet.add(TWO);
        }
    }

    //TODO: Remove smoothX.
    boolean sieve(long offsetX, ArrayList<Long> smoothX, float[] qLogValues, BigInteger N) {
        int factorBasePrimesSize = factorBasePrimes.size();
        for (int i = 0; i < sieveCurrentX.length; i++) {
            //TODO: Check if maybe long
            long x = sieveCurrentX[i];
            int prime = sievePrimeOffset[i];
            while (x < offsetX + qLogValues.length) {
                sieveCurrentX[i] += prime;
                int relativeX = (int) (x - offsetX);

                if (qLogValues[relativeX] < Float.POSITIVE_INFINITY) {
                    qLogValues[relativeX] -= primeLogValues[i];

                    if (qLogValues[relativeX] < threshHold) {
                        BigInteger qValue = Q(BigInteger.valueOf(x), N);
                        long q = 0;
                        boolean useLong = false;
                        for (int col = 0; col < factorBasePrimesSize; col++) {

                            if (useLong || qValue.bitLength() < 64) {
                                if(!useLong){
                                    q = qValue.longValue();
                                    useLong = true;
                                }

                                row[col] = 0;
                                long p = factorBasePrimes.get(col);

                                while (q % p == 0) {
                                    q = q / p;
                                    row[col] = (byte) (row[col] == 1 ? 0 : 1);
                                }
                                if (q == 1) {
                                    matrix[smoothX.size()] = row;
                                    row = new byte[row.length];
                                    smoothX.add(x);
                                    break;
                                }
                            } else {
                                row[col] = 0;
                                long p = factorBasePrimes.get(col);

                                BigInteger BIGprime = BigInteger.valueOf(p);
                                BigInteger[] qr = qValue.divideAndRemainder(BIGprime);
                                while (qr[1].equals(BigInteger.ZERO)) {
                                    qValue = qr[0];
                                    row[col] = (byte) (row[col] == 1 ? 0 : 1);
                                    qr = qValue.divideAndRemainder(BIGprime);
                                }
                                if (qValue.equals(BigInteger.ONE)) {
                                    matrix[smoothX.size()] = row;
                                    row = new byte[row.length];
                                    smoothX.add(x);
                                    break;
                                }
                            }
                        }


                        if (smoothX.size() >= factorBasePrimesSize + SMOOTH_EXTRAS) {
                            return true;
                        }

                        qLogValues[relativeX] = Float.POSITIVE_INFINITY;
                    }
                }


                x = sieveCurrentX[i];
            }
        }

        //        for (long x : suspiciosSmoothX) {
        //
        //            int relativeX = (int) (x -offsetX);
        //            float qLogValue = qLogValues[relativeX];
        //            if (qLogValue < SMOOTH_ZERO) {
        //                smoothX.add(x);
        //                qLogValues[relativeX] = Float.POSITIVE_INFINITY;
        //            } else if (qLogValue < threshHold) {
        //                BigInteger qValue = Q(BigInteger.valueOf(x), N);
        //                for (int prime : factorBasePrimes) {
        //                    BigInteger BIGprime = BigInteger.valueOf(prime);
        //                    BigInteger[] qr = qValue.divideAndRemainder(BIGprime);
        //                    while (qr[1].equals(BigInteger.ZERO)) {
        //                        qValue = qr[0];
        //                        qr = qValue.divideAndRemainder(BIGprime);
        //                    }
        //                    if (qValue.equals(BigInteger.ONE)) {
        //                        smoothX.add(x);
        //                        break;
        //                    }
        //                }
        //                qLogValues[relativeX] = Float.POSITIVE_INFINITY;
        //            }
        //        }
        //        if (smoothX.size() >= factorBasePrimes.size() + SMOOTH_EXTRAS) {
        //            return true;
        //        }

        if (System.currentTimeMillis() > timeLimit) {
            return false;
        }

        long newOffsetX = offsetX + qLogValues.length;
        initQValues(newOffsetX, qLogValues, N);
        return sieve(newOffsetX, smoothX, qLogValues, N);
    }


    private int bruteForceXValuesFor2(BigInteger N) {

        //IMPORTANT! Must be called before field is changed in initQValues!
        BigInteger Q = qValueForLastX;
        if (Q.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            return 0;
        } else {
            return 1;
        }
    }

    byte[][] buildMatrix(ArrayList<Long> smoothX, BigInteger N) {

        byte[][] oldMatrix = new byte[smoothX.size()][factorBasePrimes.size()];
        for (int row = 0; row < oldMatrix.length; row++) {
            //TODO Fix matrix in Sieve
            BigInteger qValue = Q(BigInteger.valueOf(smoothX.get(row)), N);
            for (int col = 0; col < oldMatrix[0].length; col++) {
                boolean odd = false;
                BigInteger prime = BigInteger.valueOf(factorBasePrimes.get(col));
                BigInteger[] qr = qValue.divideAndRemainder(prime);

                while (qr[1].equals(BigInteger.ZERO)) {
                    qValue = qr[0];
                    odd = !odd;
                    qr = qValue.divideAndRemainder(prime);
                }
                oldMatrix[row][col] = (byte) (odd ? 1 : 0);
            }
        }
        return oldMatrix;
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

    BigInteger[] finalize(byte[][] matrix, boolean[] markedPos, BigInteger N, ArrayList<Long> smoothX) {
        ArrayList<Integer> unmarkedList = new ArrayList<Integer>();
        for (int i = 0; i < markedPos.length; i++) {
            if (!markedPos[i]) {
                unmarkedList.add(i);
            }
        }

        for (long val = 1; val < Math.pow(2, unmarkedList.size()); val++) {
            if (System.currentTimeMillis() > timeLimit) {
                return null;
            }
            boolean[] oneSetRows = new boolean[matrix.length];
            boolean[] selectedRows = new boolean[matrix.length];
            long unmarkedBitVal = 1;
            for (int unmarked = 0; unmarked < unmarkedList.size(); unmarked++) {
                if ((unmarkedBitVal & val) == unmarkedBitVal) {
                    oneSetRows[unmarkedList.get(unmarked)] = true;
                    selectedRows[unmarkedList.get(unmarked)] = true;
                }
                unmarkedBitVal = unmarkedBitVal << 1;
            }
            for (int col = 0; col < matrix[0].length; col++) {
                int colValue = 0;
                int dependentRow = -1;
                for (int row = 0; row < matrix.length; row++) {
                    if (matrix[row][col] == 1) {
                        if (oneSetRows[row]) {
                            //Increase the value
                            colValue = (colValue + 1) % 2;
                        } else if (markedPos[row]) {
                            //We have the row that is dependent and should only appear once in the matrix
                            dependentRow = row;
                        }
                    }
                }
                if (dependentRow != -1) {
                    if (colValue == 1) {
                        selectedRows[dependentRow] = true;
                    }
                } else if (colValue != 0) {
                    throw new RuntimeException("We found no dependent value but we have a total column value other than 0");
                }
            }


            BigInteger result = GCD(N, selectedRows, smoothX);
            if (!result.equals(N) && !result.equals(BigInteger.ONE)) {
                return new BigInteger[]{ result, N.divide(result) };
            }
        }

        return null;
    }


    BigInteger GCD(BigInteger N, boolean[] selectedRows, ArrayList<Long> smoothX) {
        BigInteger qValue = BigInteger.ONE;
        BigInteger xValue = BigInteger.ONE;
        BigInteger rootN = rootValForN;
        for (int row = 0; row < selectedRows.length; row++) {
            if (selectedRows[row]) {
                qValue = qValue.multiply(Q(BigInteger.valueOf(smoothX.get(row)), N));
                xValue = xValue.multiply((rootN.add(BigInteger.valueOf(smoothX.get(row)))));
            }
        }
        qValue = sqrt(qValue);
        BigInteger fuckingFinalResult = N.gcd(xValue.subtract(qValue));
        return fuckingFinalResult;
    }

    static boolean didWork;

    public static BigInteger sqrt(BigInteger A) {
        BigInteger temp = A.shiftRight(BigInteger.valueOf(A.bitLength()).shiftRight(1).intValue()), result = null;
        while (true) {
            result = temp.add(A.divide(temp)).shiftRight(1);
            if (!temp.equals(result)) { temp = result; } else { break; }
        }
        didWork = false;
        if (result.multiply(result).equals(A)) { didWork = true; }

        return result;
    }
}
