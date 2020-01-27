/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.util.concurrent.SynchronousQueue;

/**
 * @class BitonicPipeline class per cpsc5600 hw3 specification.
 * @version 24-Jan-2020
 */
public class BitonicPipeline {
   public static final int N = 1 << 22;  // size of the final sorted array (power of two)
   public static final int TIME_ALLOWED = 10;  // seconds

   public static final int NUM_THREADS = 4;
   /**
    * Main entry for parallel portion of HW3 assignment.
    *
    * @param args not used
    */
   public static void main(String[] args) {
      long start = System.currentTimeMillis();
      int work = 0;
      while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000) {

         double[][] data = new double[NUM_THREADS][];


         SynchronousQueue<double[]> randomArrayQueue = new SynchronousQueue<>(), sortedArrayQueue = new SynchronousQueue<>();

         // Create 4 random array generators
         for(int section = 0; section < data.length; section++) {
            Thread randomArrayGeneratorThread = new Thread(new RandomArrayGenerator(N/4, randomArrayQueue), "Random Array Gen Thread #" + (section+1));
            randomArrayGeneratorThread.start();
         }

         // Create the 4 StageOne threads
         for(int section = 0; section < data.length; section++) {

            //input.put(data[section] = RandomArrayGenerator.getArray(N/4));

            Thread stageOneThread = new Thread(new StageOne(randomArrayQueue, sortedArrayQueue), "Stage One Thread #" + (section+1));
            stageOneThread.start();
         }


         // Create 2 BitonicStage Threads
         for(int section = 0; section < 2; section++) {
            Thread bitonicThread = new Thread(new BitonicStage(), "Bitonic Stage Thread #" + (section+1));

         }








         // Note that BitonicStage assumes both its input arrays are sorted
         // increasing. It then inverts its second input to form a true bitonic
         // sequence from the concatenation of the first input with the inverted
         // second input.
         /*
         BitonicStage bitonic = new BitonicStage();
         double[] penult1 = bitonic.process(data[0], data[1]);
         double[] penult2 = bitonic.process(data[2], data[3]);
         double[] ult = bitonic.process(penult1, penult2);
         if (!RandomArrayGenerator.isSorted(ult) || N != ult.length)
            System.out.println("failed");
          */
         work++;
      }
      System.out.println("sorted " + work + " arrays (each: " + N + " doubles) in "
            + TIME_ALLOWED + " seconds");
   }
}