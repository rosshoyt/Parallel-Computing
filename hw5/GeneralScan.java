package hw5;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * Generalized reducing/scanning class with methods for preparing the data elements into
 * TallyType objects, combining two TallyType objects, and initializing the beginning TallyType
 * object. These methods can be overridden by subclasses to do any kind of operation for the
 * reduce/scan.
 *
 * @param <ElemType></ElemType>   This is the data type of the read-only data elements.
 * @param <TallyType> This is the combination-result data type. This type must have a 0-arg ctor.
 *                    Should Default to ElemType.
 * @param <ResultType></ResultType> This is the final result data type. Any final tally will be converted to this
 *                    data type (using the gen(tally) method).
 *                    Should default to TallyType.
 */
public abstract class GeneralScan<ElemType, TallyType, ResultType> {

   /**
    * Interior node number of the root of the parallel reduction.
    */
   public static int ROOT = 0;

   /**
    * Default number of threads to use in the parallelization.
    */
   public static int N_THREADS = 16;  // fork a thread for top levels


   /**
    * ForkJoinPool used by the reduce and scan operations
    */
   private ForkJoinPool forkJoinPool;

   /**
    * Construct the reducer/scanner with the given input. Sets n_threads to default.
    * @param raw input data
    */
   public GeneralScan(List<ElemType> raw){
      this(raw, N_THREADS);
   }

   /**
    * Construct the reducer/scanner with the given input.
    * @param rawData      input data
    * @param n_threads    number of threads to use for parallelization, defaults to N_THREADS
    */
   public GeneralScan(List<ElemType> rawData, int n_threads){
      this.reduced = false;
      this.rawData = rawData;
      this.n = rawData.size();
      this.height = (int)Math.ceil(Math.log(n)/Math.log(2));
      this.n_threads = n_threads;

      // validate input data
      if (1 << height != n)
         throw new IllegalArgumentException("data size must be power of 2 for now"); // FIXME
      if (n_threads >= n)
         throw new IllegalArgumentException("must be more data than threads!");

      // initialize shared ForkJoinPool
      forkJoinPool = new ForkJoinPool(n_threads);

      // initialize scan data array
      scanData = new ArrayList<>(n);
      for(int i = 0; i < n; i++) scanData.add(null);

      // initialize tally data array
      int n_tallies = n_threads * 2;
      tallyData = new ArrayList<>(n_tallies);
      for(int i = 0; i < n_tallies; i++) tallyData.add(init());
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
    * @return the reduction for the root node
    */
   public ResultType getReduction(){
      return getReduction(ROOT);
   }

   /**
    * Get all the scan (inclusive) results for all the input data.
    */
   public List<ResultType> getScan() {
      reduced = reduced || reduce(ROOT); // need to make sure reduction has already run to get the prefix tallies
      return scan(ROOT, init());
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

   /**
    * Combine and replace left with result.
    * (Note this is slightly different than the POPP textbook
    * which does both combine and prepare in this method.)
    * @param accumulator the tally to combine and replace
    * @param right       the other combine operand
    */
   protected TallyType accum(TallyType accumulator, TallyType right) {
      return combine(accumulator, right);
   }

   /**
    * Flag to say if we've already done the initial reduction
    */
   private boolean reduced;

   /**
    *  The number of ElemType in rawData array
    */
   private int n;

   /**
    * The height of the heap
    */
   private int height;

   /**
    * The number of threads to use in the fork join pool
    */
   private int n_threads;

   /**
    * The raw data array to perform the Scan on
    */
   private List<ElemType> rawData;

   /**
    * The list of tally data
    */
   private List<TallyType> tallyData;

   /**
    * The scan data
    */
   private List<ResultType> scanData;

   /**
    * Get the value for a node in the tree.
    * If the node is in the interior, it has the required tally already.
    * If the node is a leaf, it has to get converted to a tally (via prepare).
    */
   private TallyType value(int i) {
      if (!isLeaf(i))
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
      forkJoinPool.invoke(new ReduceAction(i));
      return true;
   }

   /**
    * Recursive binary-tree prefix scan (inclusive).
    *
    * @param i           node number
    * @param tallyPrior  tally of all the elements to the left of this node
    * @return
    */
   private List<ResultType> scan(int i, TallyType tallyPrior) {
      forkJoinPool.invoke(new ScanAction(i, tallyPrior));
      return scanData;
   }

   /**
    * Method that calculates the total size of Heap, including interior and leaf nodes
    *
    * @return total number of heap nodes
    */
   private int size() {
      return (n - 1) + n;
   }

   /**
    * Method that calculates and returns the index of the provided index's parent node.
    *
    * @param i heap index to calculate the parent index of
    * @return  index of parent node
    */
   private int parent(int i) {
      return (i - 1) / 2;
   }

   /**
    * Method that calculates and returns the global index of the left child node of the
    * provided global tree index
    *
    * @param i  heap index to calculate the left child index of
    * @return   left child index
    */
   private int left(int i) {
      return i * 2 + 1;
   }

   /**
    * Method that calculates and returns the global index of the right child node of the
    * provided global tree index
    *
    * @param i  heap index to calculate the right child index of
    * @return   left child index
    */
   private int right(int i) {
      return left(i) + 1;
   }

   /**
    * Method that checks if a given heap index is a leaf
    *
    * @param i index to check
    * @return true if index is leaf index
    */
   private boolean isLeaf(int i) {
      return left(i) >= size();
   }

   /**
    * Method that returns the left-most heap index of a provided index
    *
    * @param i  index to check
    * @return   the leftmost heap index
    */
   private int leftmost(int i) {
      while (!isLeaf(i))
         i = left(i);
      return i;
   }

   /**
    * Method that returns the right-most heap index of a provided index
    *
    * @param i  index to check
    * @return   the right-most heap index
    */
   private int rightmost(int i) {
      while (!isLeaf(i))
         i = right(i);
      return i;
   }

   /**
    * Inner class which extends RecursiveAction and is used by the
    * ForkJoinPool to execute the reduce operation in parallel
    */
   class ReduceAction extends RecursiveAction {

      private int index;

      /**
       * Constructs a RecursiveReduceAction
       * @param index starting tree index of this part of the reduce
       */
      ReduceAction(int index) {
         this.index = index;
      }

      /**
       * The main computation performed by this task.
       */
      @Override
      protected void compute() {
         process(index);
      }

      /**
       * Recursive method which performs the reduce operation
       * and creates recursive helper actions
       * @param i subtree index
       */
      private void process(int i){
         if (i < n_threads - 1) {
            ForkJoinTask.invokeAll(new ReduceAction(left(i)),
                                   new ReduceAction(right(i)));
            tallyData.set(i, combine(value(left(i)), value(right(i))));
         } else {
            TallyType tally = init();
            int rm = rightmost(i);
            for (int j = leftmost(i); j <= rm; j++)
               tally = accum(tally, value(j));
            tallyData.set(i, tally);
         }
      }
   }

   /**
    * Inner class that extends RecursiveAction and is used by the
    * ForkJoinPool to execute the scan operation in parallel
    */
   class ScanAction extends RecursiveAction {

      private int index;
      private TallyType tallyPrior;

      /**
       * Constructs a RecursiveReduceAction
       * @param index starting tree index of this part of the reduce
       */
      public ScanAction(int index, TallyType tallyPrior) {
         this.index = index;
         this.tallyPrior = tallyPrior;
      }

      /**
       * The main computation performed by this task.
       */
      @Override
      protected void compute() {
         process(index, tallyPrior);
      }

      /**
       * Recursive method for Scan
       * @param i
       * @param tallyPrior
       */
      private void process(int i, TallyType tallyPrior) {
         if (i < n_threads - 1) {
            ForkJoinTask.invokeAll(new ScanAction(left(i), tallyPrior),
                  new ScanAction(right(i), combine(tallyPrior, value(left(i)))));
         } else {
            TallyType tally = tallyPrior;
            int rm = rightmost(i);
            for (int j = leftmost(i); j <= rm; j++) {
               tally = accum(tally, value(j));
               scanData.set(j - (n - 1), gen(tally));
            }
         }
      }
   }
}

