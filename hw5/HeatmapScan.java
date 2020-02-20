package hw5;
/*
 * Ross Hoyt
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */
import java.awt.*;
import java.util.List;


public class HeatmapScan extends GeneralScan<Observation, HeatmapFrame, HeatmapFrame> {

   private int height, width;

   public int getMaxHits(){
      HeatmapFrame finalFrame = getReduction();
      getScan();
      int maxHits = 0;
      for(int x = 0; x < finalFrame.frameGrid.length; x++)
         for(int y = 0; y < finalFrame.frameGrid[x].length; y++)
            if(finalFrame.frameGrid[x][y] > maxHits) maxHits = finalFrame.frameGrid[x][y];
      return maxHits;
   }
   /**
    * Constructor for HeatmapScan which uses a default level of parallelization
    * @param height 
    * @param width
    * @param observations  input data
    */
   public HeatmapScan(List<Observation> observations, int n_threads, int width, int height) {
      super(observations);
      this.width = width;
      this.height = height;
   }

   /**
    * Construct the reducer/scanner with the given input.
    *
    * @param observations  input data
    * @param n_threads     number of threads to use in parallelization
    */
   public HeatmapScan(List<Observation> observations, int n_threads) {
      super(observations, n_threads);
   }

   /**
    * Identity element for tally operation.
    * So, combine(init(), prepare(x)) == prepare(x).
    * Typically for summing, the return is 0, for products, 1.
    * @return identity tally element
    */
   @Override
   protected HeatmapFrame init() {
      return new HeatmapFrame(height, width);
   }

   /**
    * Convert an element (in the input data) to a tally.
    * Typically, if ElemType and TallyType are the same, this just returns datum.
    *
    * @param datum the datum to be converted
    * @return the corresponding tally
    */
   @Override
   protected HeatmapFrame prepare(Observation datum) {

      HeatmapFrame frame = new HeatmapFrame(width, height);
      frame.addObservation(datum);
      return frame;
   }

   /**
    * Combine two tallies.
    * Tallies should be commutative, i.e., combine(a,b) == combine(b,a)
    * For summing, this typically returns left + right.
    * @param left   one of the tallies to combine
    * @param right  the other of the tallies to combine
    * @return       a new tally which is the combination of left and right
    */
   @Override
   protected HeatmapFrame combine(HeatmapFrame left, HeatmapFrame right) {
      HeatmapFrame frame = new HeatmapFrame(width, height);
      frame.addObservationList(left.observations);
      frame.addObservationList(right.observations);
      return frame;
      // TODO optimize... & remove need to have public list of observations in heatmap frame??
      // ...
//      for(int i = 0, hitsAdded = 0; i < width; i++){
//         for(int j = 0; j <  height || hitsAdded >= right.getNumObservations(); j++)
//            if(i < )
//      }

   }
   /**
    * Convert a tally to a result.
    * If the ResultType and TallyType are the same, typically this returns tally.
    * @param tally  the resultant tally to be converted
    * @return       the result indicated by the tally
    */
   @Override
   protected HeatmapFrame gen(HeatmapFrame tally) {
      return tally;
   }

   @Override
   protected HeatmapFrame accum(HeatmapFrame accumulator, HeatmapFrame right){
      for(Observation o: right.observations) accumulator.addObservation(o);
      return accumulator;
   }


}
