package hw3;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @class BitonicPipeline class per cpsc5600 hw3 specification.
 * @version 24-Jan-2020
 */
public class BitonicPipeline {
   public static final int N = 1 << 22,  // size of the final sorted array (power of two)
                           TIME_ALLOWED = 10,  // seconds
                           TIMEOUT = 10;  // in seconds (max wait for an output)

   private static final int N_ARRAY_GENS = 4,
                            N_STAGE_ONES = N_ARRAY_GENS,
                            N_BIT_STAGES = N_STAGE_ONES - 1,
                            N_THREADS = N_ARRAY_GENS + N_STAGE_ONES + N_BIT_STAGES,
                            N_QUEUES = N_THREADS,
                            ROOT_QUEUE = 0;

   /**
    * Main entry for parallel portion of HW3 assignment.
    *
    * @param args not used
    */
   public static void main(String[] args) {
      Thread[] threads = new Thread[N_THREADS];
      ArrayList<SynchronousQueue<double[]>> queues = new ArrayList<>();
      for (int i = 0; i < N_QUEUES; i++)
         queues.add(new SynchronousQueue<>());

      for (int i = 1; i <= N_ARRAY_GENS; i++) // queues ->10, ->9, ->8, ->7
         threads[N_THREADS - i] = new Thread(new RandomArrayGenerator(N / 4, queues.get(N_QUEUES - i)));

      for (int i = 1; i <= N_STAGE_ONES; i++) // queues for I 10->6, II 9->5, III 8->4, IV 7->3
         threads[N_THREADS - N_ARRAY_GENS - i] = new Thread(new StageOne(
               queues.get(N_QUEUES - i),
               queues.get(N_QUEUES - N_ARRAY_GENS - i), "one" + i));

      for (int i = 0; i < N_BIT_STAGES; i++) // queues for VII (1,2)->0, V (3,4)->1, VI (5,6)->2
         threads[i] = new Thread(new BitonicStage(
               queues.get(2 * i + 1), queues.get(2 * i + 2),  // inputs are the children
               queues.get(i), "bit" + i));

      for (int i = 0; i < threads.length; i++)
         threads[i].start();

      long start = System.currentTimeMillis();
      int work = 0;
      double[] ult = null;

      while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000) {
         try {
            ult = queues.get(ROOT_QUEUE).poll(TIMEOUT * 1000, TimeUnit.MILLISECONDS);
         } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
         }
         if (ult == null)
            System.out.println("BitonicPipeline produced null array");
         else if (!RandomArrayGenerator.isSorted(ult))
            System.out.println("BitonicPipeline produced unsorted array");
         else
            work++;
      }
      for (Thread thread : threads)
         thread.interrupt();
      System.out.println("sorted " + work + " arrays (each: " + ult.length + " doubles) in "
            + TIME_ALLOWED + " seconds");

   }
}