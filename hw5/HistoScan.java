package hw5;

import java.util.List;

class Histo {
   public static final int N = 10;
   public int[] bucket;
   public int hi, lo;

   public Histo() {
      this.bucket = new int[N + 2];
      this.hi = 100;
      this.lo = 0;
   }
}

public class HistoScan extends GeneralScan<Integer, Histo, Histo> {

   /**
    * Construct the reducer/scanner with the given input. Sets n_threads to default.
    *
    * @param raw input data
    */
   public HistoScan(List<Integer> raw) {
      super(raw);
   }

   /**
    * Construct the reducer/scanner with the given input.
    *
    * @param raw       input data
    * @param n_threads number of threads to use for parallelization, defaults to N_THREADS
    */
   public HistoScan(List<Integer> raw, int n_threads) {
      super(raw, n_threads);
   }

   /**
    * Identity element for tally operation.
    * So, combine(init(), prepare(x)) == prepare(x).
    * Typically for summing, the return is 0, for products, 1.
    *
    * @return identity tally element
    */
   @Override
   protected Histo init() {
      return new Histo();
   }

   /**
    * Convert an element (in the input data) to a tally.
    * Typically, if ElemType and TallyType are the same, this just returns datum.
    *
    * @param datum the datum to be converted
    * @return the corresponding tally
    */
   @Override
   protected Histo prepare(Integer datum) {
      Histo h = new Histo();
      int bucketSize = (h.hi - h.lo) / h.N;
      if (datum < h.lo)
         h.bucket[0]++;
      else if (datum >= h.hi)
         h.bucket[h.N + 1]++;
      else
         h.bucket[1 + (datum - h.lo) / bucketSize]++;
      return h;
   }

   /**
    * Combine two tallies.
    * Tallies should be commutative, i.e., combine(a,b) == combine(b,a)
    * For summing, this typically returns left + right.
    *
    * @param left  one of the tallies to combine
    * @param right the other of the tallies to combine
    * @return a new tally which is the combination of left and right
    */
   @Override
   protected Histo combine(Histo left, Histo right) {
      Histo h = new Histo();
      for (int i = 0; i < h.N + 2; i++)
         h.bucket[i] = left.bucket[i] + right.bucket[i];
      return h;
   }

   /**
    * Convert a tally to a result.
    * If the ResultType and TallyType are the same, typically this returns tally.
    *
    * @param tally the resultant tally to be converted
    * @return the result indicated by the tally
    */
   @Override
   protected Histo gen(Histo tally) {
      return tally;
   }
}
