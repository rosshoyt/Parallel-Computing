package hw4;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

/**
 * Main class which tests Synchronized Bitonic Sorter.
 */
public class Main {
   // testing variables
   private static final int N = 1 << 22;
   private static final int N_THREADS = 16;
   private static final int GRANULARITY = 4;
   private static final int TIME_ALLOWED = 10;  // in seconds
   /**
    * Main method which which tests the SynchronizedBitonicSorter
    * @param args not used
    */
   public static void main(String[] args) {
      SynchronizedBitonicSorter sorter = new SynchronizedBitonicSorter();
      // start timer
      long start = System.currentTimeMillis();
      int work = 0;

      while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000) {
         try {
            double[] result = sorter.sort(RandomArrayGenerator.getArray(N), N_THREADS, GRANULARITY);
            if (!RandomArrayGenerator.isSorted(result) || N != result.length)
               System.out.println("failed");
            work++;
         } catch (InterruptedException e) {
            e.printStackTrace();
            return;
         }
      }
      System.out.println("T = " + TIME_ALLOWED + " seconds");
      System.out.println("N = " + N);
      System.out.println("P = " + N_THREADS);
      System.out.println("GRANULARITY = " + GRANULARITY + " levels");
      System.out.println(work + " arrays of doubles sorted");
   }
}
