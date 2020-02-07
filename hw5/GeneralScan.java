package hw5;
/**
 * @file GeneralScan.java - fully recursive version of a generic parallelized reduce/scan
 * @author Ross Hoyt
 * @version 6-Feb-2020
 */
import java.util.List;
import java.util.ArrayList;
/**
 * Generalized reducing/scanning class with methods for preparing the data elements into
 * TallyType objects, combining two TallyType objects, and initializing the beginning TallyType
 * object. These methods can be overridden by subclasses to do any kind of operation for the
 * reduce/scan.
 *
 * @tparam ElemType   This is the data type of the read-only data elements.
 * @tparam TallyType  This is the combination-result data type. This type must have a 0-arg ctor.
 *                    Defaults to ElemType.
 * @tparam ResultType This is the final result data type. Any final tally will be converted to this
 *                    data type (using the gen(tally) method). Defaults to TallyType.
 */
public abstract class GeneralScan<ElemType, TallyType, ResultType > {
   /**
    * Interior node number of the root of the parallel reduction.
    */
   public static int ROOT = 0;

   /**
    * Default number of threads to use in the parallelization.
    */
   public static int N_THREADS = 16;  // fork a thread for top levels

   /**
    * Construct the reducer/scanner with the given input. Sets n_threads to default.
    * @param raw input data
    */
   public GeneralScan(List<ElemType> raw){
      this(raw, N_THREADS);
   }
   /**
    * Construct the reducer/scanner with the given input.
    * @param raw          input data
    * @param n_threads    number of threads to use for parallelization, defaults to N_THREADS
    */
   public GeneralScan(List<ElemType> raw, int n_threads){
      reduced= false;
      n = raw.size();
      rawData = raw;
      height = (int)Math.ceil(Math.log(n)/Math.log(2));
      this.n_threads = n_threads;

      if (1 << height != n)
         throw new IllegalArgumentException("data size must be power of 2 for now"); // FIXME
      tallyData = new ArrayList<TallyType>(n - 1);
   }

   /**
    * Get the result of the reduction at any node. The algorithm computes and saves the complete
    * reduction once and then serves subsequent requests from the stored results.
    * Node numbers are in a binary tree level ordering starting at ROOT of 0.
    * @param i    node number (defaults to ROOT)
    * @return     the reduction for the given node
    * @throws IllegalArgumentException if the node number is invalid
    */
   public ResultType getReduction(int i) {
      if (i >= size())
         throw new IllegalArgumentException("non-existent node");
      reduced = reduced || reduce(ROOT); // can't do this is in ctor or virtual overrides won't work
      return gen(value(i));
   }

   /**
    * Gets the result of the reduction at the root node.
    * @return
    */
   public ResultType getReduction(){
      return this.getReduction(ROOT);
   }

   /**
    * Get all the scan (inclusive) results for all the input data.
    * @param output  scan results (vector is indexed corresponding to input elements)
    */
   public void getScan(List<ResultType> output) {
      reduced = reduced || reduce(ROOT); // need to make sure reduction has already run to get the prefix tallies
      scan(ROOT, init(), output);
   }



   /**
    * Identity element for tally operation.
    * So, combine(init(), prepare(x)) == prepare(x).
    * Typically for summing, the return is 0, for products, 1.
    * @return identity tally element
    */
   protected abstract TallyType init();

   /**
    * Convert an element (in the input data) to a tally.
    * Typically, if ElemType and TallyType are the same, this just returns datum.
    * @param datum  the datum to be converted
    * @return       the corresponding tally
    */
   protected abstract TallyType prepare(ElemType datum);

   /**
    * Combine two tallies.
    * Tallies should be commutative, i.e., combine(a,b) == combine(b,a)
    * For summing, this typically returns left + right.
    * @param left   one of the tallies to combine
    * @param right  the other of the tallies to combine
    * @return       a new tally which is the combination of left and right
    */
   protected abstract TallyType combine(TallyType left, TallyType right);

   /**
    * Convert a tally to a result.
    * If the ResultType and TallyType are the same, typically this returns tally.
    * @param tally  the resultant tally to be converted
    * @return       the result indicated by the tally
    */
   protected abstract ResultType gen(TallyType tally);


   private boolean reduced;  // flag to say if we've already done the initial reduction
   private int n; // n is size of data, n-1 is size of interior
   private List<ElemType> rawData;
   private List<TallyType> tallyData;
   private int height;
   private int n_threads;

   /**
    * Get the value for a node in the tree.
    * If the node is in the interior, it has the required tally already.
    * If the node is a leaf, it has to get converted to a tally (via prepare).
    */
   private TallyType value(int i) {
      if (i < n - 1)
         return tallyData.get(i);
      else
         return prepare(rawData.get(i - (n - 1)));
   }

   /**
    * Recursive pair-wise reduction.
    * @param i  node number
    * @return   true
    */
   private boolean reduce(int i) {
      if (!isLeaf(i)) {
         if (i < n_threads - 1) {
            //auto handle = std::async(std::launch::async, &GeneralScan::reduce, this, left(i));
            reduce(right(i));
            //handle.wait();
         } else {
            reduce(left(i));
            reduce(right(i));
         }
         tallyData.set(i, combine(value(left(i)), value(right(i))));
      }
      return true;
   }

   /**
    * Recursive binary-tree prefix scan (inclusive).
    * @param i           node number
    * @param tallyPrior  tally of all the elements to the left of this node
    * @param output      where to write the output results
    */
   private void scan(int i, TallyType tallyPrior, List<ResultType> output) {
      if (isLeaf(i)) {
         output.set(i - (n - 1), gen(combine(tallyPrior, value(i))));
      } else {
         if (i < n_threads - 1) {
            //auto handle = std::async(std::launch::async, &GeneralScan::scan, this, left(i), tallyPrior, output);
            scan(right(i), combine(tallyPrior, value(left(i))), output);
            //handle.wait();
         } else {
            scan(left(i), tallyPrior, output);
            scan(right(i), combine(tallyPrior, value(left(i))), output);
         }
      }
   }

   // Following are for maneuvering around the binary tree
   private int size() {
      return (n - 1) + n;
   }

   private int parent(int i) {
      return (i - 1) / 2;
   }

   private int left(int i) {
      return i * 2 + 1;
   }

   private int right(int i) {
      return left(i) + 1;
   }

   private boolean isLeaf(int i) {
      return left(i) >= size();
   }
};

