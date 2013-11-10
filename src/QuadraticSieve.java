import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class QuadraticSieve {
    static final int FACTOR_BASE_LIMIT_B_CONSTANT_C = 3;
    int sieveSpan; //TODO: Use log of biggest element in factor base instead
    static final float SMOOTH_ZERO   = (float) Math.sqrt(47);
    static final int   SMOOTH_EXTRAS = 1;

    private double factorBaseLimitB;

    private List<Integer> factorBasePrimes;
    int[]      sieveCurrentX;
    int[]      sievePrimeOffset;
    float[]    sievePrimeLog;
    BigInteger rootValForN;

    public QuadraticSieve() {}
    public QuadraticSieve(BigInteger N) {
        init(N);
    }

    public void init(BigInteger N) {
        factorBasePrimes = new ArrayList<Integer>();
        rootValForN = PrimeDivider.root(2, N, true);
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
        //TODO test:  (int) Math.log(factorBasePrimes.get(getFactorBasePrimes().size()-1));
        sieveSpan = 1000;

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
    public boolean isSmooth(BigInteger value) {
        if (value.equals(BigInteger.ONE)) {
            return true;
        }
        //        if (value > -SMOOTH_ZERO && value < SMOOTH_ZERO) {
        //            return true;
        //        }
        return false;
    }

    public BigInteger Q(BigInteger x, BigInteger N) {
        //Using long to floor the value.
        //TODO OPTIMIZE THIS MOTHERFUCKER
        BigInteger rootVal = rootValForN;

        //TODO: I'm guessing this should be integer and not decimal.
        BigInteger quad = ((rootVal.add(x)).multiply(rootVal.add(x)));
        BigInteger result = quad.subtract(N);


        //        BigInteger result =  x.multiply(x).subtract(N);


        return result;
    }

    //    public BigInteger root(BigInteger value) {
    //        return PrimeDivider.root(2, value, true);
    //    }

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

    private void initQValues(ArrayList<BigInteger> qValues, int size, BigInteger N) {
        qValues.ensureCapacity(size);

        for (int x = qValues.size(); x < size; x++) {
            qValues.add(x, Q(BigInteger.valueOf(x), N));
        }
    }

    public ArrayList<Integer> sieve(BigInteger N, long timeLimit) {
        int sieveSize;
        if (factorBasePrimes.contains(2)) {
            sieveSize = factorBasePrimes.size() * 2 - 1;
        } else {
            sieveSize = factorBasePrimes.size() * 2;
        }
        sievePrimeLog = new float[sieveSize];
        sievePrimeOffset = new int[sieveSize];
        sieveCurrentX = new int[sieveSize];

        //Special for prime 2!

        int pos = 0;
        for (int i = 0; i < factorBasePrimes.size(); i++) {
            int prime = factorBasePrimes.get(i);
            if (prime == 2) {
                sievePrimeOffset[pos] = prime;
                sievePrimeLog[pos] = (float) Math.log(prime);
                sieveCurrentX[pos] = bruteForceXValuesFor2(N);
                pos++;
            } else {
                int[] xArray = tonelliShanks(N, prime);
                if(System.currentTimeMillis() > timeLimit){
                    return null;
                }
                float logPrime = (float) Math.log(prime);
                for (int x : xArray) {
                    sievePrimeOffset[pos] = prime;
                    sievePrimeLog[pos] = logPrime;
                    sieveCurrentX[pos] = x;
                    pos++;
                }
            }
        }


        ArrayList<Integer> smoothX = new ArrayList<Integer>(factorBasePrimes.size() + SMOOTH_EXTRAS);
        ArrayList<BigInteger> qValues = new ArrayList<BigInteger>(sieveSpan);
        initQValues(qValues, sieveSpan, N);
        if (sieve(smoothX, qValues, N, timeLimit)) {
            return smoothX;
        } else {
            return null;
        }
    }

    //TODO: Precalculate this shit (tonelliShanks
    boolean sieve(ArrayList<Integer> smoothX, ArrayList<BigInteger> qValues, BigInteger N, long timeLimit) {
        for (int i = 0; i < sieveCurrentX.length; i++) {
            int x = sieveCurrentX[i];
            int prime = sievePrimeOffset[i];
            while (x < qValues.size()) {
                sieveCurrentX[i] += prime;
                BigInteger qValue = qValues.get(x);
                BigInteger BIGprime = BigInteger.valueOf(prime);
                BigInteger[] qr = qValue.divideAndRemainder(BIGprime);
                while (qr[1].equals(BigInteger.ZERO)) {
                    qValue = qr[0];
                    qr = qValue.divideAndRemainder(BIGprime);
                }

                qValues.set(x, qValue);
                if (isSmooth(qValues.get(x))) {
                    smoothX.add(x);
                    if (smoothX.size() >= factorBasePrimes.size() + SMOOTH_EXTRAS) {
                        return true;
                    }
                }
                x = sieveCurrentX[i];
            }
        }
        if (System.currentTimeMillis() > timeLimit) {
            return false;
        }

        initQValues(qValues, qValues.size() + sieveSpan, N);
        return sieve(smoothX, qValues, N, timeLimit);
    }

    private int bruteForceXValuesFor2(BigInteger N) {
        BigInteger Q = Q(BigInteger.ZERO, N);
        if (Q.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            return 0;
        } else {
            return 1;
        }
    }

    byte[][] buildMatrix(ArrayList<Integer> smoothX, BigInteger N) {
        byte[][] matrix = new byte[smoothX.size()][factorBasePrimes.size()];
        for (int row = 0; row < matrix.length; row++) {
            BigInteger qValue = Q(BigInteger.valueOf(smoothX.get(row)), N);
            for (int col = 0; col < matrix[0].length; col++) {
                boolean odd = false;
                BigInteger prime = BigInteger.valueOf(factorBasePrimes.get(col));
                BigInteger[] qr = qValue.divideAndRemainder(prime);

                while (qr[1].equals(BigInteger.ZERO)) {
                    qValue = qr[0];
                    odd = !odd;
                    qr = qValue.divideAndRemainder(prime);
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

    BigInteger[] finalize(byte[][] matrix, boolean[] markedPos, BigInteger N, ArrayList<Integer> smoothX, long timeLimit) {
        ArrayList<Integer> unmarkedList = new ArrayList<Integer>();
        for (int i = 0; i < markedPos.length; i++) {
            if (!markedPos[i]) {
                unmarkedList.add(i);
            }
        }

        for (long val = 1; val < Math.pow(2, unmarkedList.size()); val++) {
            if(System.currentTimeMillis() > timeLimit){
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
                return new BigInteger[]{result, N.divide(result)};
            }
        }

        return null;
    }


    BigInteger GCD(BigInteger N, boolean[] selectedRows, ArrayList<Integer> smoothX) {
        BigInteger qValue = BigInteger.ONE;
        BigInteger xValue = BigInteger.ONE;
        BigInteger rootN = rootValForN;
        for (int row = 0; row < selectedRows.length; row++) {
            if (selectedRows[row]) {

                qValue = qValue.multiply(Q(BigInteger.valueOf(smoothX.get(row)), N));
                xValue = xValue.multiply((rootN.add(BigInteger.valueOf(smoothX.get(row)))));
            }
        }
        qValue = PrimeDivider.root(2, qValue, false);
        BigInteger fuckingFinalResult = N.gcd(xValue.subtract(qValue));
        return fuckingFinalResult;
    }
}
