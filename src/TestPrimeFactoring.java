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
    public void testLegendre(){
        QuadraticSieve qs = new QuadraticSieve();
        int residue = qs.legendre(BigInteger.valueOf(6), 11);
        assertEquals(residue, -1);
    }

    @Test
    public void testTS(){
        QuadraticSieve qs = new QuadraticSieve();
        int[] xs = qs.tonelliShanks(BigInteger.valueOf(10), 13);
        assertEquals(xs[1], 2);
        assertEquals(xs[0], 3);
    }

//    @Test
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

//    @Test
//    public void testQSMatrices(){
//        QuadraticSieve quadraticSieve = new QuadraticSieve();
//        byte[][] test = {{0,0,0,1},{1,1,1,0},{1,1,1,1}};
//        //        byte[][] test = {{1,1,0,0},{1,1,0,1},{0,1,1,1},{0,0,1,0},{0,0,0,1}};
//        boolean[] marked = new boolean[test.length];
//        quadraticSieve.gaussElimination(test, marked);
//        boolean[] selected = quadraticSieve.finalize(test, marked);
//        ArrayList<Integer> xSmooth = new ArrayList<Integer>();
//        xSmooth.add(0);
//        xSmooth.add(3);
//        xSmooth.add(71);
//
//        long result = quadraticSieve.finalizeGCD(BigInteger.valueOf(15347), selected, xSmooth).longValue();
//        assertEquals(result, 103);
//    }

    //@Test
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

    @Test
    public void fullTestQS(){
        BigInteger N = BigInteger.valueOf(15347);
//        BigInteger N = BigInteger.valueOf(1621984134912629L);
//        BigInteger N = BigInteger.valueOf(62615533L);
//        BigInteger N = BigInteger.valueOf(9797);
//        BigInteger N = BigInteger.valueOf(911121L);

        QuadraticSieve qs = new QuadraticSieve();
        qs.calculateFactorBaseLimitB(N);
        qs.calculateFactoreBase(N);
        List<Integer> baseFactors = qs.getFactorBasePrimes();
//        assert(baseFactors.size() >= 4);
//        assertEquals(baseFactors.get(0).intValue(), 2);
//        assertEquals(baseFactors.get(1).intValue(), 17);
//        assertEquals(baseFactors.get(2).intValue(), 23);
//        assertEquals(baseFactors.get(3).intValue(), 29);

        ArrayList<Integer> smoothX = qs.sieve(N);
//        assertEquals(smoothX.size(), baseFactors.size()+QuadraticSieve.SMOOTH_EXTRAS);

        byte[][] matrix = qs.buildMatrix(smoothX,N);
        boolean[] marked = new boolean[matrix.length];

        qs.gaussElimination(matrix, marked);
        BigInteger result = qs.finalize(matrix, marked, N, smoothX);
        for(int col = 0; col < matrix[0].length; col++){
            for(int row = 0; row < matrix.length; row++){
                if(matrix[row][col] == 1){
                    System.out.print(row + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

}
