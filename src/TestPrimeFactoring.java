import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author mathiaslindblom
 */
public class TestPrimeFactoring {
    private PrimeDivider pd = new PrimeDivider();

    @Test
    public void testPerfectPotenses(){
        testPerfectPotenses(PrimeTable1.TABLE);
        testPerfectPotenses(PrimeTable2.TABLE);
        testPerfectPotenses(PrimeTable3.TABLE);
        testPerfectPotenses(PrimeTable4.TABLE);
    }

    private void testPerfectPotenses(int[] table) {
        for(int i : table){
            BigInteger value = BigInteger.valueOf(i);
            for(int root = 2; root <= PrimeDivider.PERFECT_POTENS_MAX_ROOT; root++){
                pd.init(value.pow(root));
                assert(pd.perfektPotens());
                assertEquals(pd.getFoundPrimes().size(), root);
                for(BigInteger prime: pd.getFoundPrimes()){
                    assertEquals(prime, value);
                }
            }
        }
    }


}
