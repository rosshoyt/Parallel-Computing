package hw5;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import org.junit.*;
import org.junit.runners.MethodSorters;
import utils.RandomArrayGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import static java.util.stream.Collectors.toList;

/**
 * Test of GeneralScan.java using the simple concrete implementation DoubleHeap.java
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeneralScanTest {
   // size of test
   private static final int N = 1<<22;
   // num threads
   private static final int P = 16;
   // required Double precision during Assert.equals() comparisons
   private final double DELTA = 1e-4;

   // the list of random values to use
   private static List<Double> randomList;

   // the reference results to be calculated
   private static Double sequentialReductionResult;
   private static List<Double> sequentialScanResult;

   // Simple, concrete implementation of Heap
   private static DoubleSumHeap doubleHeap;

   @BeforeClass
   public static void setUp() throws Exception {
      randomList = DoubleStream.of(RandomArrayGenerator.getArray(N)).boxed().collect(toList());
      doubleHeap = new DoubleSumHeap(randomList, P);
      // calculate reference results sequentially
      sequentialReductionResult = getReductionSequentially(randomList);
      sequentialScanResult = getScanSequentially(randomList);
   }

   @Test
   public void getReduction() {
      Double result = doubleHeap.getReduction();
      Assert.assertEquals(result, sequentialReductionResult, DELTA);
   }

   @Test
   public void getScan() {
      List<Double> result = doubleHeap.getScan();
      for(int i = 0; i < N; i++)
         Assert.assertEquals(result.get(i), sequentialScanResult.get(i), DELTA);
   }

   /**
    * test util that returns the reduction (sum) of a list using Java stream API
    * @param list
    * @return reduction
    */
   private static Double getReductionSequentially(List<Double> list){
      return list.stream().mapToDouble(Double::doubleValue).sum();
   }

   /**
    * test util that returns the reduction (sum) of a list using Java stream API
    * @param inputList
    * @return scan data
    */
   private static List<Double> getScanSequentially(List<Double> inputList){
      List<Double> results = new ArrayList<>(inputList.size());
      results.add(inputList.get(0));
      for(int i = 1; i < inputList.size(); i++)
         results.add(inputList.get(i) + results.get(i - 1));
      return results;
   }
}