package hw5;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import java.util.List;

public class DoubleSumHeap extends GeneralScan<Double, Double, Double>{
   public DoubleSumHeap(List<Double> raw) {
      super(raw);
   }

   public DoubleSumHeap(List<Double> raw, int numThreads){ super(raw, numThreads); }

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

   /**
    * Combine and replace left with result.
    * (Note this is slightly different than the POPP textbook
    * which does both combine and prepare in this method.)
    * @param accumulator the tally to combine and replace
    * @param right       the other combine operand
    */
   @Override
   protected Double accum(Double accumulator, Double right) {
      return combine(accumulator, right);
   }
}