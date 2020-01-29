      /*
       * Ross Hoyt
       * CPSC 5600, Seattle University
       * This is free and unencumbered software released into the public domain.
       */
      import java.util.concurrent.SynchronousQueue;
      import java.util.concurrent.TimeUnit;

      /**
       * @class BitonicStage.java
       * @author Ross Hoyt
       * @see "Seattle University, CPSC5600, Winter 2020"
       *
       */
      public class BitonicStage implements Runnable {
         private static final int timeout = 10;  // in seconds

         private enum UpDown { UP, DOWN }

         private double[] data = null;
         private SynchronousQueue<double[]> input1, input2, output;
         private String name;

         /**
          * Default constructor used in sequential version of program
          */
         public BitonicStage() { }

         /**
          *  The Runnable part of the class. Polls the input queues and when ready, process (sort)
          *  it and then write it to the output queue.
          */
         @Override
         public void run() {
            double[] arr1 = new double[1], arr2 = new double[1];
            while (arr1 != null) {
               try {
                  arr1 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                  arr2 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                  if (arr1 != null && arr2 != null) {
                     double[] outArr = process(arr1, arr2);
                     output.offer(outArr, timeout * 1000, TimeUnit.MILLISECONDS);
                  } else {
                     System.out.println(getClass().getName() + " " + name + " got null array");
                  }
               } catch (InterruptedException e) {
                  return;
               }
            }
         }

         /**
          * Method that takes two sorted increasing input arrays, inverts the second input array,
          * and forms true bitonic sequence. Then sorts bitonically
          *
          * @param half1 Sorted, increasing array 1
          * @param half2 Sorted, increasing array 2
          * @return true bitonic sequence formed from input arrays
          */
         public double[] process(double[] half1, double[] half2) {
            int inputLen = half1.length + half2.length;
            if(data == null || data.length != inputLen)
               data = new double[inputLen];

            int i = 0;
            for (int j = 0; j < half1.length; j++)
               data[i++] = half1[j];

            bitonic_sort(0, data.length, UpDown.UP);
            return data;
         }


         private void bitonic_sort(int start, int n, UpDown dir) {
            if(n > 1) {
               int half = n / 2;
               bitonic_merge(start, n, dir);
               bitonic_sort(start, half, dir);
               bitonic_sort(start+half, half, dir);

            }
         }

         private void bitonic_merge(int start, int n, UpDown dir) {
            if (n > 1) {
               int half = n / 2;
               if (dir == UpDown.UP) {
                  for (int i = start; i < start + half; i++)
                     if (data[i] > data[i + half])
                        swap(i, i + half);

               } else {
                  for (int i = start; i < start + half; i++)
                     if (data[i] < data[i + half])
                        swap(i, i + half);
               }
            }
         }

         private void swap(int i, int j) {
            if(i != j) {
               double temp = data[i];
               data[i] = data[j];
               data[j] = temp;
            }
         }

         /**
          * Private helper method that inverts a double array
          * Used by method process() on to invert its second input array
          * @param data
          */
         private void invert(double[] data) {
            int j = data.length - 1;
            for(int i = 0; i < data.length; i++){
               double temp = data[i];
               data[i] = data[j];
               data[j] = temp;
            }
         }

         /**
          * Set up a BitonicStage with a SynchronousQueue to read for input and one to
          * write for output.
          *
          * @param input1  where to read the unordered input array
          * @param output where to write the sorted array
          */
         public BitonicStage(SynchronousQueue<double[]> input1, SynchronousQueue<double[]> input2,
                             SynchronousQueue<double[]> output) {
            this(input1, input2, output, "");
         }

         /**
          * Set up a BitonicStage with a SynchronousQueue to read for input and one to
          * write for output.
          *
          * @param input1  where to read the unordered input array
          * @param output where to write the sorted array
          * @param name   the name of the thread
          */
         public BitonicStage(SynchronousQueue<double[]> input1, SynchronousQueue<double[]> input2,
                             SynchronousQueue<double[]> output, String name) {
            this.input1 = input1;
            this.input2 = input2;
            this.output = output;
            this.name = name;
         }


      }

