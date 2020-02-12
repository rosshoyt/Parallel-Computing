package hw5;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import utils.RandomArrayGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import static java.util.stream.Collectors.toList;

/**
 * Test of GeneralScan.java using the simple concrete implementation DoubleHeap.java
 */
public class GeneralScanTest {
   private final int N = 1<<5;
   private final int P = 8;
   private final double DELTA = 1e-10;

   private List<Double> randomList;

   private Double sequentialReductionResult;
   private List<Double> sequentialScanResult;

   // Test Heap
   private DoubleHeap doubleHeap;

   @Before
   public void setUp() throws Exception {
      randomList = DoubleStream.of(RandomArrayGenerator.getArray(N)).boxed().collect(toList());
      doubleHeap = new DoubleHeap(randomList, P);
      // initialize sequential reference results
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