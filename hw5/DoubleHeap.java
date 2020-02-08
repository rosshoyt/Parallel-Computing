package hw5;

import java.util.List;

public class DoubleHeap extends GeneralScan<Double, Double, Double>{
   public DoubleHeap(List<Double> raw) {
      super(raw);
   }

   @Override
   protected Double init() {
      return 0.0;
   }

   @Override
   protected Double prepare(Double datum) {
      return datum;
   }

   @Override
   protected Double combine(Double left, Double right) {
      return left + right;
   }

   @Override
   protected Double gen(Double tally) {
      return tally;
   }
}