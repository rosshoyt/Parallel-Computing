package hw4;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Class which runs a synchronized bitonic sort on a provided array with its sort() method
 */
public class SynchronizedBitonicSorter {
   /**
    * Sorts a bitonic array in parallel using synchronized threads
    * @param data array to sort
    * @param nThreads number of threads to sort with
    * @param granularity how many barriers used
    * @return data array sorted
    * @throws InterruptedException
    *
    */
   public double[] sort(double[] data, int nThreads, int granularity) throws InterruptedException, IllegalArgumentException {
      // first assert granularity < log2(data.length)
      if(granularity < Math.log(data.length)/Math.log(2)) {
         // construct heap of barriers
         // barrierheap[0] is across all of n wires (j is n/2)
         // barrierHeap[1] is for wires 0..n/2-1 (j is n/4)
         // barrierHeap[2] is for wires n/2..n-1
         // barrierHeap[3] is for wires 0..n/4-1 (j is n/8)
         // etc..
         CyclicBarrier[] barrierHeap = new CyclicBarrier[(1 << granularity) - 1];
         //int width = data.length; // for debug message below
         int threadsPerBarrier = nThreads;
         int i = 1;
         for (int node = 0; node < barrierHeap.length; node++) {
            if (node == i) {
               //width /= 2;
               i = i * 2 + 1;
               threadsPerBarrier /= 2;
            }
            //System.out.println("Making barrier[" + node + "], " + width + " wide, for " + threadsPerBarrier + " threads");
            barrierHeap[node] = new CyclicBarrier(threadsPerBarrier);
         }

         // Make and start all threads
         List<Thread> threads = new ArrayList<>(nThreads);
         for (int t = 0; t < nThreads; t++) {
            Thread thread = new Thread(new BitonicWorkerThread(data, t, nThreads, barrierHeap));
            threads.add(thread);
            thread.start();
         }
         // Wait for all threads
         for (Thread thread : threads)
            thread.join();

         return data;
      }
      else throw new IllegalArgumentException("Granularity must be < log2(data.length)");
   }

   /**
    * Thread class which is a worker in the bitonic sort
    */
   private static class BitonicWorkerThread implements Runnable {
      private int threadIndex, startIndex, endIndex;
      double[] data;
      CyclicBarrier[] barrierHeap;

      /**
       * Constructs a symmetric worker thread
       * @param data shared data
       * @param threadIndex which thread this is
       * @param nThreads total num threads
       * @param barrierHeap shared barriers for synchronization
       */
      BitonicWorkerThread(double[] data, int threadIndex, int nThreads, CyclicBarrier[] barrierHeap) {
         this.data = data;
         this.threadIndex = threadIndex;
         this.barrierHeap = barrierHeap;
         // set thread indices
         int nLocalElements = data.length / nThreads;
         this.startIndex = threadIndex * nLocalElements;
         this.endIndex = startIndex + nLocalElements;
      }

      @Override
      public void run() {
         // k is size of the pieces, starting at pairs and doubling up until we get to the whole array
         // k also determines if we want ascending or descending for each section of i's
         for (int k = 2; k <= data.length; k *= 2) { // k is one bit, marching to the left
            // j is the distance between the first and second halves of the merge
            for (int j = k / 2; j > 0; j /= 2) {  // j is one bit, marching from k to the right
               awaitBarrier(j);
               // i is the merge element
               for (int i = startIndex; i < endIndex; i++) {
                  int ixj = i ^ j;  // xor: all the bits that are on in one and off in the other
                  // only compare if ixj is to the right of i
                  if (ixj > i) {
                     if ((i & k) == 0)
                        compareAndSwapUp(i, ixj);
                     else
                        compareAndSwapDown(i, ixj);
                  }
               }
            }
         }
      }

      private void awaitBarrier(int j) {
         // check if barrier is necessary
         // (only when the sweep is not within the thread's range of i)
         int jsweep = j*2;
         int size = endIndex - startIndex;
         int n = data.length;
         if(jsweep<size)
            return;

         // choose the smallest granularity barrier available that spans our jsweep
         int width = n/2, i = 1;
         while(width > jsweep && i < barrierHeap.length) {
            width /= 2;
            i = i*2 + 1;
         }
         width *= 2;
         i /= 2;

         // Each barrier is for width wires; left most heap node at this level is node i
         // Of the barriers at this level, we need the one that serves for our seep
         // Thus, find the offset so that width*offset <= start < width*(offset-1)
         int offset = startIndex/width;
         int node = i + offset;
         //System.out.println("Chose barrier[" + node + "] for j=" + j + ", start=" + start);
         try {
            barrierHeap[node].await();
         } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println(threadIndex + " broken barrier due to: " + e);
         }
      }

      private void compareAndSwapUp(int a, int b) {
         if(data[a] > data[b])
            swap(a, b);
      }

      private void compareAndSwapDown(int a, int b) {
         if(data[a] < data[b])
            swap(a, b);
      }

      private void swap(int index1, int index2){
         double temp = data[index2];
         data[index1] = data[index2];
         data[index2] = temp;
      }

   }
}

