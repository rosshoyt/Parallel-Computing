package hw5;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class GeneralScanTest {

   class SumHeapDemo extends GeneralScan<Integer, Integer, Integer>{
      public SumHeapDemo(List<Integer> raw) {
         super(raw);
      }

      @Override
      protected Integer init() {
         return 0;
      }

      @Override
      protected Integer prepare(Integer datum) {
         return datum;
      }

      @Override
      protected Integer combine(Integer left, Integer right) {
         return left + right;
      }

      @Override
      protected Integer gen(Integer tally) {
         return tally;
      }
   }

   int[] testVals = {
     1,1,1,1,
     1,1,1,1,
     1,1,1,1,
     1,1,1,2
   };
   @Test
   public void getReduction() {

      SumHeapDemo sumHeap = new SumHeapDemo(Arrays.stream(testVals).boxed().collect(Collectors.toList()));
      assert sumHeap.getReduction() == 17;

   }

   @Test
   public void testGetReduction() {

   }

   @Test
   public void getScan() {
   }
}