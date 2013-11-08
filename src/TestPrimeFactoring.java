import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * @author mathiaslindblom
 */
public class TestPrimeFactoring {
    private PrimeDivider pd = new PrimeDivider();

//    @Test
    public void testPerfectPotenses(){
        testPerfectPotenses(PrimeTable1.TABLE);
//        testPerfectPotenses(PrimeTable2.TABLE);
//        testPerfectPotenses(PrimeTable3.TABLE);
//        testPerfectPotenses(PrimeTable4.TABLE);
    }

    private void testPerfectPotenses(int[] table) {
        for(int i : table){
            BigInteger value = BigInteger.valueOf(i);
            for(int root = 2; root <= PrimeDivider.PERFECT_POTENS_MAX_ROOT; root++){
                pd.init(value.pow(root));
                assert(pd.potensFinder());
                List<BigInteger> list = new ArrayList<BigInteger>();
                for(int a = 0; a < root; a++){
                    list.add(value);
                }
                testFoundPrimes(list);
            }
        }
    }
    @Test
    public void testQS(){
        BigInteger testValue = BigInteger.valueOf(100);
        QuadraticSieve qs = new QuadraticSieve();
        qs.calculateFactorBaseLimitB(testValue);
        qs.calculateFactoreBase(testValue);
        List<Integer> baseFactors = qs.getFactorBasePrimes();
        assertEquals(baseFactors.size(), 4);
        assertEquals(baseFactors.get(0).intValue(), 2);
        assertEquals(baseFactors.get(1).intValue(), 3);
        assertEquals(baseFactors.get(2).intValue(), 7);
        assertEquals(baseFactors.get(3).intValue(), 11);
    }

    @Test
    public void testQSMatrices(){
        QuadraticSieve quadraticSieve = new QuadraticSieve();
        byte[][] test = {{0,0,0,1},{1,1,1,0},{1,1,1,1}};
        //        byte[][] test = {{1,1,0,0},{1,1,0,1},{0,1,1,1},{0,0,1,0},{0,0,0,1}};
        boolean[] marked = new boolean[test.length];
        quadraticSieve.gaussElimination(test, marked);
        boolean[] selected = quadraticSieve.findLeftNullSpaceVector(test, marked);
        ArrayList<Integer> xSmooth = new ArrayList<Integer>();
        xSmooth.add(0);
        xSmooth.add(3);
        xSmooth.add(71);

        long result = quadraticSieve.finalizeGCD(BigInteger.valueOf(15347), selected, xSmooth).longValue();
        assertEquals(result, 103);
    }

    @Test
    public void testPollard(){
        BigInteger val = new BigInteger("784365874326589234654325634829563285923");
        pd.init(val);
        assert(pd.pollard(val, System.currentTimeMillis() + 100000, 1));
        List<BigInteger> list = new ArrayList<BigInteger>();
        list.add(new BigInteger("270049004600029939628263"));
        list.add(new BigInteger("20393697827"));
        list.add(new BigInteger("1951"));
        list.add(new BigInteger("73"));
        testFoundPrimes(list);

    }

    private void testFoundPrimes(List<BigInteger> shouldBe){
        assertEquals(pd.getFoundPrimes().size(), shouldBe.size());
        boolean success;
        for(BigInteger val: pd.getFoundPrimes()){
            success = false;
            for(int i = 0; i < shouldBe.size(); i++){
                if(val.compareTo(shouldBe.get(i)) == 0){
                    shouldBe.remove(i);
                    success = true;
                    break;
                }
            }
            assert (success);
        }
    }
}
