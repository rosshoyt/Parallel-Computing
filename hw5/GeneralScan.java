package hw5;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
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

      forkJoinPool = new ForkJoinPool(n_threads);

      int nTallies = n - 1;
      tallyData = new ArrayList<TallyType>(nTallies);

      for(int i = 0; i < nTallies; i++) tallyData.add(init());

      //System.out.println("Created GeneralScan for N = " + n + " total size = " + size());
   }

   /**
    * Get the result of the reduction at any node. The algorithm computes and saves the complete
    * reduction once and then serves subsequent requests from the stored results.
    * Node numbers are in a binary tree level ordering starting at ROOT of 0.
    * @param i    node number (defaults to ROOT)
    * @return     the reduction for the given node
    * @throws IllegalArgumentException if the node number is invalid
    */
   ResultType getReduction(int i) {
      if (i >= size())
         throw new IllegalArgumentException("non-existent node");
      reduced = reduced || reduce(ROOT); // can't do this is in ctor or virtual overrides won't work
      ResultType res = gen(value(i));
      System.out.println("Reduction result = " + res);
      return res; //gen(value(i));
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
      ForkJoinPool forkJoinPool = new ForkJoinPool(n_threads);
      forkJoinPool.invoke(new RecursiveReduceAction(rawData, tallyData, i));
      return true;
   }

   /**
    * Recursive binary-tree prefix scan (inclusive).
    * TODO Implement
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

   /**
    * Inner class which extends RecursiveAction and is used by the
    * ForkJoinPool to execute the reduce() operation in parallel
    */
   class RecursiveReduceAction extends RecursiveAction {
      private int index;
      private List<ElemType> raw;
      private List<TallyType> tallies;
      /**
       * Constructs a RecursiveReduceAction
       * @param index starting tree index of this part of the reduce
       */
      RecursiveReduceAction(List<ElemType> rawData, List<TallyType> tallyData, int index) {
         raw = rawData;
         tallies = tallyData;
         this.index = index;
         System.out.println("RecursiveReduceAction created for index " + index);
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
         if (!isLeaf(i)) {
            if (i < n_threads - 1) {
               forkJoinPool.invoke(new RecursiveReduceAction(raw, tallies, left(i)));
               process(right(i));
            } else {
               System.out.println(Thread.currentThread().getName() + "Processing sequentially i > n_threads-1");
               process(left(i));
               process(right(i));
            }
            TallyType t = combine(value(left(i)), value(right(i)));
            System.out.println(Thread.currentThread().getName() + " Setting tallies[" + i + "] = " + t);
            tallies.set(i, t);//combine(value(left(i)), value(right(i))));
         }
         else System.out.println(Thread.currentThread().getName() + " at Leaf node " + i);
      }
   }
}

