package hw4;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *
 */
public class SynchronizedBitonicSorter {
   static final int N = 1 << 4;
   static final int N_THREADS = 4;
   static final int TIME_ALLOWED = 2;//10;  // seconds

   static CyclicBarrier cyclicBarrier;
   static ArrayList<Thread> threads;




   private static double[] sort(double[] data){


      // init threads




      return data;
   }

   private static class BitonicWorkerThread implements Runnable {
      private int startingIndex, finalIndex;

      BitonicWorkerThread(int startingIndex, int finalIndex) {
         this.startingIndex = startingIndex;
         this.finalIndex = finalIndex;
      }

      @Override
      public void run() {
         System.out.println(Thread.currentThread().getName() + " start/final indices: " + startingIndex + ", " + finalIndex);
         try{
            System.out.println(Thread.currentThread().getName() + " is waiting");
            cyclicBarrier.await();
            System.out.println(Thread.currentThread().getName() + " has crossed the barrier");
         } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
         }

      }
   }

//   private static class BarrierAction implements Runnable {
//      @Override
//      public void run() {
//         System.out.println("All threads at barrier!");
//      }
//   }
   /**
    * Main method which which tests the Synchornized Bitonic Sort
    *
    * @param args not used
    */
   public static void main(String[] args) {

      //cyclicBarrier = new CyclicBarrier(N_THREADS, new BarrierAction());
      cyclicBarrier = new CyclicBarrier(N_THREADS);
      threads = new ArrayList<>();
      int nPerThread = N / N_THREADS;
      for(int i = 0; i < N_THREADS; i++) {
         int startIndex = i * nPerThread, endIndex = startIndex + nPerThread - 1;
         threads.add(new Thread(new BitonicWorkerThread(startIndex, endIndex), "Thread " + (i + 1)));
      }

      // start threads
      for(Thread t : threads) t.start();

      // start timer
      long start = System.currentTimeMillis();
      int work = 0;

      while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000) {
         double[] result = sort(RandomArrayGenerator.getArray(N));



         if (!RandomArrayGenerator.isSorted(result) || N != result.length)
            //System.out.println("failed");
         work++;
      }
      System.out.println("sorted " + work + " arrays (each: " + N + " doubles) in "
            + TIME_ALLOWED + " seconds");
   }

}

