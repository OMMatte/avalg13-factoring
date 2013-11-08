import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class PrimeDivider {
    //Shortcut values used in code.
    private final static BigInteger ZERO = BigInteger.ZERO;
    private final static BigInteger ONE  = BigInteger.ONE;
    private final static BigInteger TWO  = new BigInteger("2");

    //Secure random to be used by pollard factor.
    private final static SecureRandom random = new SecureRandom();

    //BigInteger will guess if prime with certainty 1-(1/2^CONSTANT). 7 => 99.2 %
    public static final int IS_PRIME_CERTAINTY = 7;

    //The number of prime tables to be used by trial division algorithm.
    public static final int TRIAL_DIVISION_PRIME_TABLES = 2;

    //The number of roots to be tried with potensFinder. 2 ... Constant.
    public static final int PERFECT_POTENS_MAX_ROOT = 6;

    //If we should try to find potenses after a algorithm has found a prime (not for trialDivision or potens search itself
    public static final boolean TRY_POTENS_SEARCH_AFTER_ADD = true;

    //The amount of milliseconds the pollard factor algorithm should spend on a single value.
    public static final long TIME_LIMIT = 100;

    //The current value to factorize. Will be changed to the remainding value to factorize each
    //time a prime factor have been found and added to foundPrimes list.
    private BigInteger currentValue;

    //The list of found primes for the initial currentValue.
    private List<BigInteger> foundPrimes;

    //The number of potenses the intial currentValue consisted of. If this is > 1 it means
    //that the intial currentValue has been split up amountPerfectPotenses times.
    //This variable should be considered when adding primes to foundPrimes.
    //TODO: Take this into account when adding primes everywhere.
    private int totalAmountPotenses;

    /**
     * Creates the foundPrimes list.
     */
    public PrimeDivider() {
        foundPrimes = new ArrayList<BigInteger>();
    }

    /**
     * Prepares the class to factorize the given value. Clears the primes list and updates
     * currentValue to the given value. Also resets totalAmountPotenses.
     *
     * @param value The value to be prepared to later be factorized.
     */
    void init(BigInteger value) {
        currentValue = value;
        foundPrimes.clear();
        totalAmountPotenses = 1;
    }

    /**
     * Calls init with given value and afterward calls factorize, which will factorize the value.
     *
     * @return true if fully factorized. false otherwise.
     */
    public boolean factorize(BigInteger value) {
        //Just init and factorize.
        init(value);
        return factorize();
    }

    /**
     * Performs algorithms necessary to factorize currentValue. Updates the foundPrimes list
     * with factors found.
     *
     * @return true if fully factorized. false otherwise.
     */
    boolean factorize() {
        //If the value is 1 or probably a prime number, then factorization is not needed.
        if (preFactorize()) {
            return true;
        }


        //Try to factorize by using trialDivision algorithm.
        //        if (trialDivision()) {
        //            return true;
        //        }

        //        if (trialDivision()) {
        //            return true;
        //        }

        QuadraticSieve qs = new QuadraticSieve();
        qs.calculateFactorBaseLimitB(currentValue);
        qs.calculateFactoreBase(currentValue);
        for (int i = 1; i < 100; i++) {
            qs.tonelliShanks(currentValue, qs.getFactorBasePrimes().get(i));
        }


        if (pollard(currentValue, System.currentTimeMillis() + TIME_LIMIT, totalAmountPotenses)) {
            return true;
        }

        if (potensFinder()) {
            return true;
        }

        return false;
    }

    /**
     * Performs trial division with the number of prime tables as set by TRIAL_DIVISION_PRIME_TABLES.
     * Updates foundPrimes and currentValue.
     *
     * @return true if currentValue is fully factorized. Otherwise false.
     */
    private boolean trialDivision() {
        //If the value is 1 or probably a prime number, then factorization is not needed.
        if (preFactorize()) {
            return true;
        }

        try {
            //Perform a trial division for each prime table.
            for (int i = 1; i <= TRIAL_DIVISION_PRIME_TABLES; i++) {
                //Get the table from the current table class.
                Class<?> tableClass = Class.forName("PrimeTable" + i);
                Field f = tableClass.getField("TABLE");
                int[] table = (int[]) f.get(tableClass);

                //Perform the trialDivision with the current table.
                //If it returns true, the currentValue has been fully factorized, so return.
                if (trialDivision(table)) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        //The currentValue has not been fully factorized.
        return false;
    }

    /**
     * The trial division algorithm.
     * <p/>
     * Tries to divide the currentValue with primes (as found in tables).
     * If no remainder is present after division, the divisor is a factor.
     * <p/>
     * If a factor is found, it is added to the foundPrimes and currentValue
     * is updated to the remainding value that is yet to be factorized.
     *
     * @param table The prime table to be used while performing trial division.
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
                    addPrime(prime);

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
            addPrime();
            return true;
        }

        //The currentValue is not fully factorized if this is reached.
        return false;
    }

    /**
     * The potens finder algorithm.
     * <p/>
     * Tries to factorize the given value into a non-decimal value.
     * Will calculate roots of the currentValue up to a given constant until no more roots are to be found.
     * For the value 10000, the root 100 will first be discovered, then the value 10 and then it will find no more.
     * <p/>
     * Perform PERFECT_POTENS_MAX_ROOT number of root searches.
     * <p/>
     * Updates currentValue and foundPrimes list if primes are found.
     *
     * @return PotensResult containing the new value, times divided and a boolean stating if we found primes.
     */
    PotensResult potensFinder(BigInteger value, int amountPotenses) {

        boolean rootFound = true;

        //Loop as long as roots are found.
        while (rootFound) {
            //Set the rootFound to false so that we need to have actually found roots
            //in order to continue loop.
            rootFound = false;

            //Perform the n:th-roots as defined by PERFECT_POTENS_MAX_ROOT.
            for (int n = 2; n <= PERFECT_POTENS_MAX_ROOT; n++) {

                //Get the n:th-root of currentValue.
                BigInteger x = root(n, value, false);

                //Check if the root was calculated successfully.
                boolean isRoot = !x.equals(ZERO);
                if (isRoot) {
                    //The root x was calculated successfully. Update the variable that holds the number
                    //of factors the root is of currentValue, and set currentValue of to the root.
                    amountPotenses *= n;
                    value = x;

                    //Indicate that a root has been found, to keep trying to split currentValue up into roots.
                    //Break, because currentValue has been changed, and we want to start over with n:th-root checking.
                    rootFound = true;
                    break;
                }
            }

            //Check if currentValue is a probable prime.
            if (value.isProbablePrime(IS_PRIME_CERTAINTY)) {
                //It is, which means that the initial currentValue has currentValue as factors as
                //determined by totalAmountPotenses.

                //Add currentValue to the foundPrimes list totalAmountPotenses times.
                addPrime(value, false, amountPotenses * totalAmountPotenses);

                //Return true to indicate that the currentValue is fully factorized.
                return new PotensResult(value, amountPotenses, true);
            }
        }

        //No (more) roots have been found. Also, the final value did not turn in to a prime.
        //currentValue could have been splitted and totalAmountPotenses updated.
        return new PotensResult(value, amountPotenses, false);
    }

    /**
     * This method uses the currentValue and totalAmountPotenses as parameters to potensFinder.
     * Updates totalAmountPotenses to indicate how many root splits have been made. This should be taken
     * into account when finding primes of currentValue that have previously been splitted by potensFinder.
     *
     * @return True if we found primes.
     */
    boolean potensFinder() {
        PotensResult pr = potensFinder(currentValue, totalAmountPotenses);
        currentValue = pr.value;
        totalAmountPotenses = pr.amountPotenses;
        return pr.lastValueIsPrimeAndAdded;
    }

    private class PotensResult {
        public BigInteger value;
        public int        amountPotenses;
        public boolean    lastValueIsPrimeAndAdded;

        public PotensResult(BigInteger value, int amountPotenses, boolean lastValueIsPrimeAndAdded) {
            this.value = value;
            this.amountPotenses = amountPotenses;
            this.lastValueIsPrimeAndAdded = lastValueIsPrimeAndAdded;
        }
    }

    /**
     * Performs Newton-Raphson to calculate the n:th-root of a given value.
     *
     * @param n     The n:th-root to be calculated.
     * @param value The value to calculate the n:th-root of.
     * @return An estimated n:th-root of value, or 0 if unable to find a root.
     */
    public static BigInteger root(int n, final BigInteger value, boolean alwaysReturnValue) {
        //prevX will hold the value of the previously estimated root x. Initially null.
        BigInteger prevX = null;

        //TODO: Consider better start guess than one
        BigInteger x = ONE;

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
            if (alwaysReturnValue) {
                if (sign == -1) {
                    //This means that we ALWAYS return the upper value of the root
                    x = x.add(ONE);
                }
                return x;
            }
            //We have failed to compute the root, so return 0 to indicate this.
            return ZERO;
        }

        //The root has been calculated and verified. Return it.
        return x;
    }


    /**
     * Same as pollard(currentValue, timeLimit, totalAmountPotenses)
     */
    public boolean pollard(long timeLimit) {
        return pollard(currentValue, timeLimit, totalAmountPotenses);
    }

    /**
     * Recursive Pollard Rho factoring combined with potensFinder.
     * <p/>
     * At every successful divide, a potensFinder will be conducted on the 2 new values IF they are not primes.
     * <p/>
     * Updates foundFactors and currentValue.
     *
     * @param value          The value currently being factored.
     * @param timeLimit      If System.currentTimeMillis() gets higher than this value, we abort and return false.
     * @param amountPotenses The amount to add primes duo to prior successful potensFinder operations.
     * @return True if factoring is fully completed. False otherwise but some factoring could still have occurred.
     */
    public boolean pollard(BigInteger value, long timeLimit, int amountPotenses) {
        //Checks if value is fully factorized. If it is, currentValue is updated to currentValue / value.
        if (value.compareTo(ONE) == 0) { return true; }
        if (value.isProbablePrime(IS_PRIME_CERTAINTY)) {
            addPrime(value, false, amountPotenses);
            return true;
        }

        PotensResult pr = potensFinder(value, amountPotenses);
        if (pr.lastValueIsPrimeAndAdded) {
            return true;
        } else {
            amountPotenses = pr.amountPotenses;
            value = pr.value;
        }

        //Get the divisor.
        BigInteger divisor = pollardFindDiviser(value, timeLimit);

        //Check if it is a valid divisor.
        if (divisor.compareTo(ZERO) == 0) {
            //It is not, so return false.
            return false;
        }

        //Recursively perform a pollard of the divisor, if its not a prime then do no more.
        if (!pollard(divisor, timeLimit, amountPotenses)) {
            return false;
        }

        //Keep dividing and return the result.
        return pollard(value.divide(divisor), timeLimit, amountPotenses);
    }


    /**
     * Find a divisor of value to be used by pollard.
     *
     * @param value     The value to find a divisor of.
     * @param timeLimit The exact time which the function will stop.
     * @return An divisor of value or 0 if failed.
     */
    public BigInteger pollardFindDiviser(BigInteger value, long timeLimit) {
        BigInteger divisor;
        BigInteger c = new BigInteger(value.bitLength(), random);
        BigInteger x = new BigInteger(value.bitLength(), random);
        BigInteger xx = x;

        // check divisibility by 2
        if (value.mod(TWO).compareTo(ZERO) == 0) { return TWO; }

        do {
            if (System.currentTimeMillis() > timeLimit) {return ZERO;}
            x = x.multiply(x).mod(value).add(c).mod(value);
            xx = xx.multiply(xx).mod(value).add(c).mod(value);
            xx = xx.multiply(xx).mod(value).add(c).mod(value);
            divisor = x.subtract(xx).gcd(value);
        }
        while ((divisor.compareTo(ONE)) == 0);

        return divisor;
    }

    /**
     * Adds prime to the foundPrimes list and divides currentValue with the prime value.
     *
     * @param prime          The prime to be added.
     * @param potensSearch   true if a potens search shoul be performed afterwards.
     * @param amountPotenses the amount of times the prime should be added duo to prior found potenses.
     */
    private void addPrime(final BigInteger prime, boolean potensSearch, int amountPotenses) {
        //Need to take the amount of potens splits into account. Add the prime amountPotenses number of times.
        for (int i = 0; i < amountPotenses; i++) {
            foundPrimes.add(prime);
            currentValue = currentValue.divide(prime);
        }

        //If potensSearch set to true, then perform a potens search.
        //TODO: Check that this would actually works
        if (potensSearch) {
            this.potensFinder();
        }
    }

    /**
     * Same as addPrime(prime, false, totalAmountPotenses)
     */
    private void addPrime(BigInteger prime) {
        addPrime(prime, false, totalAmountPotenses);
    }

    /**
     * Same as addPrime(currentPrime, false, totalAmountPotenses)
     */
    private void addPrime() {
        addPrime(currentValue);
    }

    /**
     * Check if the value is 1 or a probable prime.
     * <p/>
     * Updates foundFactors if value is a prime.
     * Updates currentValue to currentValue/value if value is fully factorized.
     *
     * @param prime The value to be checked if it is fully factorized.
     * @return true if value is fully factorized, false otherwise.
     */
    public boolean preFactorize(BigInteger prime) {
        //If currentValue is 1, then it cannot be factorized. Since 1 is not a prime,
        //it should not be added to the foundPrimes list.
        if (prime.compareTo(ONE) == 0) {
            return true;
        }

        //If currentValue is a probable prime, it cannot be factorized more. The currentValue
        //should be added to foundPrimes since it is a prime.
        if (prime.isProbablePrime(IS_PRIME_CERTAINTY)) {
            addPrime(prime);

            return true;
        }

        //currentValue is not 1 and not a probable prime.
        return false;
    }

    /**
     * Check if the currentValue is 1 or a probable prime.
     * <p/>
     * Updates foundFactors if currentValue is a prime.
     * Updates currentValue to 1 if fully factorized.
     *
     * @return true if currentValue is fully factorized, false otherwise.
     */
    public boolean preFactorize() {
        return preFactorize(currentValue);
    }

    /**
     * @return The foundPrimes list of found primes.
     */
    public List<BigInteger> getFoundPrimes() {
        return foundPrimes;
    }

    //TODO: Remove?
    //    public static BigDecimal takeRoot(int root, BigDecimal n, BigDecimal maxError) {
    //        int MAXITER = 5000;
    //
    //        // Specify a math context with 40 digits of precision.
    //        MathContext mc = new MathContext(40);
    //
    //        // Specify the starting value in the search for the cube root.
    //        BigDecimal x;
    //        x = new BigDecimal("1", mc);
    //
    //
    //        BigDecimal prevX = null;
    //
    //        BigDecimal rootBD = new BigDecimal(root, mc);
    //        // Search for the cube root via the Newton-Raphson loop. Output each successive iteration's value.
    //        for (int i = 0; i < MAXITER; ++i) {
    //            x = x.subtract(x.pow(root, mc)
    //                    .subtract(n, mc)
    //                    .divide(rootBD.multiply(x.pow(root - 1, mc), mc), mc), mc);
    //            if (prevX != null && prevX.subtract(x).abs().compareTo(maxError) < 0) { break; }
    //            prevX = x;
    //        }
    //
    //        return x;
    //    }
}