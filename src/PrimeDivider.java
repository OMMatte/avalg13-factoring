import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class PrimeDivider {
    private BigInteger originalValue;
    private BigInteger currentValue;
    private List<BigInteger> foundPrimes;

    public PrimeDivider() {
        foundPrimes = new ArrayList<BigInteger>();

    }

    public void init(BigInteger bi){
        originalValue = bi;
        currentValue = bi;
    }

    public boolean solve() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<BigInteger> getFoundPrimes(){
        return foundPrimes;
    }
}
