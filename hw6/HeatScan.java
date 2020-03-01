package hw6;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ross Hoyt
 *
 * Part of solution for HW6. Not working currently
 *
 *
 */
public class HeatScan extends GeneralScan<Observation, HeatMap> {


   public HeatScan(List<Observation> raw) {
      super(raw, 100);
   }

   @Override
   protected HeatMap init() {
      return new HeatMap();
   }

   @Override
   protected HeatMap prepare(Observation datum) {
      HeatMap heatMap = new HeatMap(datum.time, datum.x, datum.y);
      //timestamps.put(datum.time, heatMap);
      return heatMap;
   }

   @Override
   protected HeatMap combine(HeatMap left, HeatMap right) {
      HeatMap combinedMap = HeatMap.combine(left, right);

      return combinedMap;
   }

   @Override
   protected void accum(HeatMap hm, Observation datum) {
      hm.accum(datum.time, datum.x, datum.y);
   }


   /**
    * Altered method that tries to
    * @param samplingRate
    * @return
    */
   public List<HeatMap> getScan(int samplingRate) {

      HeatMap finalFrame = getReduction();
      for(Long time : finalFrame.timestamps.keySet())
         System.out.println(time + " " + finalFrame);


      //create ordered list for return (TODO not working)
      List<HeatMap> sortedFrames = new ArrayList<>();
      SortedSet<Long> keys = new TreeSet<Long>(finalFrame.timestamps.keySet());
      for (Long key : keys) {
         HeatMap h = finalFrame.timestamps.get(key);
         sortedFrames.add(h);
         //System.out.println(h);
      }
      return sortedFrames;
   }

}
