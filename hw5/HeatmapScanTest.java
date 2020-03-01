package hw5;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HeatmapScanTest {
   HeatmapScan heatmapScan;
   List<Observation> observationList;
   @Before
   public void setUp() throws Exception {
      observationList = new ArrayList<>();
      heatmapScan = new HeatmapScan(observationList, 4, 10, 10);
   }

   @Test
   public void getReduction() {
   }

   @Test
   public void getScan() {
   }

   @Test
   public void init() {
   }

   @Test
   public void prepare() {
   }

   @Test
   public void combine() {
   }

   @Test
   public void gen() {
   }

   @Test
   public void accum() {
   }
}