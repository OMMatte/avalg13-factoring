import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class QuadraticSieve {
    private static final int FACTOR_BASE_CONSTANT_C = 3;

    private double     factorBaseLimitB;

    public List<Integer> getFactorBasePrimes() {
        return factorBasePrimes;
    }

    private BigInteger    currentValue;
    private List<Integer> factorBasePrimes;

    public QuadraticSieve(BigInteger currentValue) {
        this.currentValue = currentValue;
        factorBasePrimes = new ArrayList<Integer>();
    }


    public void calculateFactorBaseLimitB() {
        double rootVal = PrimeDivider.takeRoot(2, new BigDecimal(currentValue), BigDecimal.ZERO).doubleValue(); //TODO: Maybe use takeRoot() with BigInteger instead, decimal preciseness might not be needed
        double logVal = 2 * Math.log(rootVal);
        double compositeLogVal = logVal * Math.log(logVal);
        double expo = 0.5 * Math.sqrt(compositeLogVal);
        double finalCalcVal = Math.pow(Math.E, expo);
        factorBaseLimitB = FACTOR_BASE_CONSTANT_C * finalCalcVal;
    }

    public void calculateFactoreBase(){
        for(int testPrime : PrimeTable1.TABLE){
            if(testPrime == 2){
                continue;
            }
            if (testPrime > factorBaseLimitB){
                break;
            }
            int residue = currentValue.pow((testPrime-1)/2).mod(BigInteger.valueOf(testPrime)).intValue();
            if(residue == 1){
                factorBasePrimes.add(testPrime);
            }
        }
    }
}
