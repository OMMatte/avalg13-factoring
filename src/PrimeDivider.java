import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class PrimeDivider {
    private final static BigInteger ZERO = BigInteger.ZERO;
    private final static BigInteger ONE  = BigInteger.ONE;
    private final static BigInteger TWO  = new BigInteger("2");

    //BigInteger will guess if prime with certainty 1-(1/2^CONSTANT). 7 => 99.2 %
    public static final int IS_PRIME_CERTAINTY  = 7;

    //The number of prime tables to be used by trial division algorithm.
    public static final int TRIAL_DIVISION_PRIME_TABLES = 2;

    //The number of roots to be tried with perfectPotens. 2 ... Constant.
    public static final int PERFECT_POTENS_MAX_ROOT = 6;

    public static final long TIME_LIMIT              = 10000;


    //The current value to factorize. Will be changed to the remainding value to factorize each
    //time a prime factor have been found and added to foundPrimes list.
    private BigInteger       currentValue;

    //The list of found primes for the initial currentValue.
    private List<BigInteger> foundPrimes;

    //TODO:
    private int amountPerfectPotenses;

    /**
     * Creates the foundPrimes list.
     */
    public PrimeDivider() {
        foundPrimes = new ArrayList<BigInteger>();
    }

    /**
     * Prepares the class to factorize the given value. Clears the primes list and updates
     * currentValue to the given value. Also resets amountPerfectPotenses.
     *
     * @param value The value to be prepared to later be factorized.
     */
    public void init(BigInteger value) {
        currentValue = value;
        foundPrimes.clear();
        amountPerfectPotenses = 1;
    }

    /**
     * Calls init with given value and afterward calls factorize, which will factorize the value.
     *
     * @return true if fully factorized. false otherwise.
     */
    public boolean factorize(BigInteger value) {
        init(value);
        return factorize();
    }

    /**
     * Performs algorithms neccesary to factorize currentValue. Updates the foundPrimes list
     * with factors found.
     *
     * @return true if fully factorized. false otherwise.
     */
    public boolean factorize() {
        //If the value is 1 or probably a prime number, then factorization is not needed.
        if (currentValue.equals(ONE) || currentValue.isProbablePrime(IS_PRIME_CERTAINTY)) {
            foundPrimes.add(currentValue);
            return true;
        }

        //Try to factorize by using trialDivision algorithm.
        if (trialDivision()) {
            return true;
        }

        if (pollardFactor(currentValue, System.currentTimeMillis() + TIME_LIMIT)) {
            return true;
        }

        //        done = perfectPotens(); //TODO: Might be totally useless, no more hits in Kattis, check the exception below
        //        if (done) {
        //            return true;
        //        }
        //        QuadraticSieve qs = new QuadraticSieve(currentValue);
        return false;
    }

    /**
     * Performs trial division with the number of prime tables as set by TRIAL_DIVISION_PRIME_TABLES.
     * Updates foundPrimes and currentValue.
     *
     * @return true if currentValue is fully factorized. Otherwise false.
     */
    private boolean trialDivision() {
        try {
            //Perform a trial division for each prime table.
            for(int i = 1; i <= TRIAL_DIVISION_PRIME_TABLES; i++) {
                //Get the table from the current table class.
                Class<?> tableClass = Class.forName("PrimeTable" + i);
                Field f = tableClass.getField("TABLE");
                int[] table = (int[])f.get(tableClass);

                //Perform the trialDivision with the current table.
                //If it returns true, the currentValue has been fully factorized, so return.
                if(trialDivision(table)) {
                    return true;
                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        //The currentValue has not been fully factorized.
        return false;
    }

    /**
     * The trial division algorithm.
     *
     * Tries to divide the currentValue with primes (as found in tables).
     * If no remainder is present after division, the divisor is a factor.
     *
     * If a factor is found, it is added to the foundPrimes and currentValue
     * is updated to the remainding value that is yet to be factorized.
     *
     * @param table The prime table to be used while performing trial division.
     *
     * @return true if currentValue is properly factorized. Otherwise false.
     */
    public boolean trialDivision(int[] table) {
        final int QUOTIENT = 0;
        final int REMAINDER = 1;

        //Perform divisions with each prime found in given table.
        for (int lp : table) {
            //Convert the prime value to a big integer.
            BigInteger prime = BigInteger.valueOf(lp);

            //If prime^2 > currentValue, then there is no way that p can be a divisor of currentValue.
            //TODO: Could also check if currentValue now is fully factorized, to break for loop early.
            if ((prime.multiply(prime)).compareTo(currentValue) == 1) {
                break;
            }

            //Try to perform currentValue / prime as longs as divisor is 0.
            while (true) {
                //Perform currentValue / prime and retrieve the quotation and remainder.
                BigInteger[] quotientAndRemainder = currentValue.divideAndRemainder(prime);

                //If the remainder is 0, then prime is a factor of currentValue.
                if (quotientAndRemainder[REMAINDER].intValue() == 0) {
                    //Update the currentValue to the quotient and add the factor to foundPrimes.
                    currentValue = quotientAndRemainder[QUOTIENT];
                    foundPrimes.add(prime);

                    //Do not break here since this prime could be multiple factors of currentValue.
                } else {
                    //The remainder is not 0, so this prime is not a divisor of currentValue.
                    //Break the division loop and try with next prime in table.
                    break;
                }
            }
        }

        //Check if the currentValue is 1 or a probable prime, which means the currentValue is fully factorized.
        if (currentValue.equals(BigInteger.ONE)) {
            //The currentValue is 1, and should therefore not be added to foundPrimes.
            return true;
        } else if (currentValue.isProbablePrime(IS_PRIME_CERTAINTY)) {
            //The currentValue is probable a prime and should therefore be added to foundPrimes.
            foundPrimes.add(currentValue);
            return true;
        }

        //The currentValue is not fully factorized if this is reached.
        return false;
    }

    /**
     * The perfect potens algorithm.
     *
     * Factorizes the currentValue by assuming that it is a potens of primes.
     * Will calculate roots of the currentValue until a prime root is found, which then is a factor.
     *
     * Perform PERFECT_POTENS_MAX_ROOT number of roots.
     *
     * Updates currentValue and foundPrimes list.
     * Updates amountPerfectPotenses to indicate how many root splits have been made. This should be taken
     * into account when finding primes of currentValue that have previously been splitted by perfectPotens.
     *
     * @return true if currentValue is fully factorized. Otherwise false.
     */
    boolean perfectPotens() {
        boolean rootFound = true;

        //Loop as long as roots are found.
        while (rootFound) {
            rootFound = false;

            //The initial starting guess n:th-root of currentValue.
            //TODO: Better start guess
            BigInteger startX = ONE;

            //Perform the n:th-roots as defined by PERFECT_POTENS_MAX_ROOT.
            for (int n = 2; n <= PERFECT_POTENS_MAX_ROOT; n++) {

                //Get the n:th-root of currentValue.
                BigInteger x = root(n, currentValue, startX);

                //Check if the root was calculated successfully.
                boolean isRoot = !x.equals(ZERO);
                if (isRoot) {
                    //The root x was calculated successfully. Update the variable that holds the number
                    //of factors the root is of currentValue, and set currentValue of to the root.
                    amountPerfectPotenses *= n;
                    currentValue = x;

                    //Indicate that a root has been found, to keep trying to split currentValue up into roots.
                    //Break, because currentValue has been changed, and we want to start over with n:th-root checking.
                    rootFound = true;
                    break;
                }
            }

            //Check if currentValue is a probable prime.
            if (currentValue.isProbablePrime(IS_PRIME_CERTAINTY)) {
                //It is, which means that the initial currentValue has currentValue as factors as
                //determined by amountPerfectPotenses.

                //Add currentValue to the foundPrimes list amountPerfectPotenses times.
                for (int i = 0; i < amountPerfectPotenses; i++) {
                    foundPrimes.add(currentValue);
                }

                //Return true to indicate that the currentValue is fully factorized.
                return true;
            }
        }
        return false;
    }

    /**
     * Performs Newton-Raphson to calculate the n:th-root of a given value.
     *
     * @param n The n:th-root to be calculated.
     * @param value The value to calculate the n:th-root of.
     * @param x The initial guess of the n:th-root of value.
     * @return An estimated n:th-root of value, or 0 if unable to find a root.
     */
    public BigInteger root(int n, final BigInteger value, BigInteger x) {
        //prevX will hold the value of the previously estimated root x. Initially null.
        BigInteger prevX = null;

        //Convert the n variable to a big integer.
        BigInteger N = BigInteger.valueOf(n);

        //Search for the n:th-root via the Newton-Raphson loop.
        //Loop as long as the newly estimated root value is not the same as previosuly estimated
        //root value (i.e. x has converged).
        while (true) {
            //Perform the new root estimation.
            x = x.subtract(x.pow(n)
                    .subtract(value)
                    .divide(N.multiply(x.pow(n - 1))));

            //Check if x has converged (i.e. it is not changed since last estimation).
            boolean converged = prevX != null && prevX.subtract(x).abs().equals(BigInteger.ZERO);
            if (converged) {
                //The estimated root has converged, so stop estimating.
                break;
            }

            //Set the previously estimated root to the current root, since another root will be estimated.
            prevX = x;
        }

        //Now the n:th-root of value has been estimated to x.
        //Validate that this is the actual n:th-root of value.

        //Perform x^n and compare it to value to check if x^n is greater, lesser or equal.
        BigInteger testN = x.pow(n);
        int sign = testN.compareTo(value);
        int startSign = sign;

        //Increment or Decrement x depending if x^n > value or x^n < value.

        //Loop while x^n != value and that the new signum is equal to the startSignum.
        //This makes sure we stop if initially x^n > value and then the new x resulted in x^n < value, for example.
        while (sign == startSign && sign != 0) {

            //Subtract x with the signum, which means x -= 1 if x^n > value and x += 1 if x^n < value.
            x = x.subtract(BigInteger.valueOf(startSign));

            //Revalidate the new x, and calculate new signum.
            testN = x.pow(n);
            sign = testN.compareTo(value);
        }

        //Either x is now the real n:th-root of value, or we have failed to compute the root.
        boolean isRoot = sign == 0;
        if (!isRoot) {
            //We have failed to compute the root, so return 0 to indicate this.
            return ZERO;
        }

        //The root has been calculated and verified. Return it.
        return x;
    }

    //TODO: Remove?
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

    private final static SecureRandom random = new SecureRandom();

    public boolean pollardFactor(BigInteger value, long timeLimit) {
        if (value.compareTo(ONE) == 0) { return true; }
        if (value.isProbablePrime(IS_PRIME_CERTAINTY)) {
            foundPrimes.add(value);
            return true;
        }
        BigInteger divisor = pollardFindDiviser(value, timeLimit);
        if (divisor.compareTo(ZERO) == 0) {return false;}

        if (!pollardFactor(divisor, timeLimit)) {return false;}

        return pollardFactor(value.divide(divisor), timeLimit);
    }


    public BigInteger pollardFindDiviser(BigInteger N, long timeLimit) {
        BigInteger divisor;
        BigInteger c = new BigInteger(N.bitLength(), random);
        BigInteger x = new BigInteger(N.bitLength(), random);
        BigInteger xx = x;

        // check divisibility by 2
        if (N.mod(TWO).compareTo(ZERO) == 0) { return TWO; }

        do {
            if (System.currentTimeMillis() > timeLimit) {return ZERO;}
            x = x.multiply(x).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            divisor = x.subtract(xx).gcd(N);
        }
        while ((divisor.compareTo(ONE)) == 0);

        return divisor;
    }

    /**
     * @return The foundPrimes list of found primes.
     */
    public List<BigInteger> getFoundPrimes() {
        return foundPrimes;
    }
}