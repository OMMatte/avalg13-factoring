import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mathiaslindblom
 */
public class Main {

    public static boolean debug = false;

    public static void main(String args[]) throws Exception {


        PrimeDivider primeDivider = new PrimeDivider();
        BufferedReader in;

        if (debug) {
            in = new BufferedReader(new FileReader("PF-100000000000000-100000000009999.txt"));
        } else {
            in = new BufferedReader(new InputStreamReader(System.in));
        }

        List<BigInteger> inputNumbers = new ArrayList<BigInteger>();
        List<List<BigInteger>> resultValues = new ArrayList<List<BigInteger>>();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.contains(";")) {
                break;
            }
            if (debug) {
                line = line.replace("*", "").replace("=", "");
                String[] lineArray = line.split("  ");
                inputNumbers.add(new BigInteger(lineArray[0]));
                ArrayList<BigInteger> result = new ArrayList<BigInteger>();
                resultValues.add(result);
                for (int i = 1; i < lineArray.length; i++) {
                    BigInteger bi = new BigInteger(lineArray[i].trim());
                    if(!bi.equals(BigInteger.ONE)){
                    result.add(new BigInteger(lineArray[i].trim()));
                    }
                }
            } else {
                inputNumbers.add(new BigInteger(line));
            }
        }

        for (int i = 0; i < inputNumbers.size(); i++) {
            BigInteger n = inputNumbers.get(i);

            if (primeDivider.factorize(n)) {
                List<BigInteger> primes = primeDivider.getFoundPrimes();
                if(debug){
                    List<BigInteger> correctResults = resultValues.get(i);
                    if(primes.size() != correctResults.size()){
                        throw new RuntimeException();
                    }
                    Collections.sort(primes);
                    Collections.sort(resultValues.get(i));
                    for(int j = 0; j < primes.size(); j++){
                        if(!primes.get(j).equals(correctResults.get(j))){
                            throw new RuntimeException();
                        }
                    }

                }
                for (BigInteger prime : primes) {
                    System.out.println(prime);
                }
            } else {
                System.out.println("fail");
            }
            if (i != inputNumbers.size() - 1) {
                System.out.println();
            }
        }
    }
}
