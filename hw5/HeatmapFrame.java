package hw5;

import java.util.ArrayList;
import java.util.List;

public class HeatmapFrame {

   public static final double CLIPPING_SPACE_MAX = 1.0;
   public static final double CLIPPING_SPACE_MIN = -1.0;
   public static final double CLIPPING_SPACE_SIZE = 2.0;

   private double xBucketSize, yBucketSize;
//   private int numXBuckets, numYBuckets;

   public int[][] frameGrid;

   private int height, width;

   public List<Observation> observations;

   public HeatmapFrame(int width, int height) {
      this.width = width;
      this.height = height;
      xBucketSize = CLIPPING_SPACE_SIZE / this.width;
      yBucketSize = CLIPPING_SPACE_SIZE / this.height;

      frameGrid = new int[width][height];

      observations = new ArrayList<>();

      for(int i = 0; i < width; i++)
         for(int j = 0; j < height; j++)
            frameGrid[i][j] = 0;

   }


   public void addObservation(Observation datum) {
      incrementGridBucket(datum.x, datum.y);
      observations.add(datum);
   }

   public void addObservationList(List<Observation> obs){
      for(Observation o: obs)
         addObservation(o);
   }

   private void incrementGridBucket(double x, double y) {
      incrementGridBucket(x,y,1);
   }


      /**
       * Method used to indicate hit(s) has occured in this heatmap frame, and to record this
       * and the
       * @param x  double x coordinate in the viewport (between -1.0 and 1.0)
       * @param y  double y coordinate in the viewport (between -1.0 and 1.0)
       */
   private void incrementGridBucket(double x, double y, int numHits) {

      int xBucket = findGridPoint(x,xBucketSize, width), yBucket = findGridPoint(y, yBucketSize, height);
      //System.out.println(String.format("Incrementing frameGrid at [%d][%d]", xBucket, yBucket));
      frameGrid[xBucket][yBucket] += numHits;

   }

   private static int findGridPoint(double coordinate, double bucketSize, int numBuckets){
      int counter = 0;
      for(double tally = CLIPPING_SPACE_MIN; tally < CLIPPING_SPACE_MAX && counter < numBuckets - 1; tally += bucketSize){
         if(coordinate <= tally)
            return counter;
         else
            counter++;
      }

      return counter;
   }
}
