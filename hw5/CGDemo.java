package hw5;
/*
 * Ross Hoyt/Kevin Lundeen
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Quick hacked-together demo of the ColorGrid and a heat map
 */
public class CGDemo {
	private static final int DIM = 150;
	private static final String REPLAY = "Replay";
	private static JFrame application;
	private static JButton button;
	private static Color[][] grid;

	private static List<HeatmapFrame> heatmapFrames;
	private static int heatmapFrameIndex = 0;
	private static int maxHits = 0;

	// Values for generating the Observations file
	private static int N_OBSERVATIONS = 256; // Must be both a power of 2, and a square number, for now (4, 16, 64, 256, 1024)
	private static int N_TIME_SLICES = 20;
	private static final String FILE_NAME = "observation_test.dat";

	static boolean testing = true; // TODO delete this temp flag which regenerates the observation file each run
	public static int N_THREADS = 16;


	public static void main(String[] args) throws FileNotFoundException, InterruptedException {

		// set observation data from existing file, or generate a new one if none exists...
		if(Files.notExists(Paths.get(FILE_NAME)) || testing) // TODO remove boolean 'testing'
			Observation.generateObservationFile(FILE_NAME, Observation.SeriesType.FULLY_RANDOM,
					N_OBSERVATIONS, N_TIME_SLICES);
		List<Observation> observations = Observation.readObservationFile(FILE_NAME);
		/*
		TODO implement general scan of the observations, and use the results to fill each display frame of heatmap
		*/
		System.out.println("Creating HeatmapScan and getting frames...");
		HeatmapScan heatMap = new HeatmapScan(observations, N_THREADS, DIM, DIM);
		heatmapFrames = heatMap.getScan();
		maxHits = heatMap.getMaxHits();
		System.out.println("Got list of " + heatmapFrames.size() + " frames, max hits was " + maxHits);

		grid = new Color[DIM][DIM];
		application = new JFrame();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fillGrid(grid, heatmapFrames.get(0));
		
		ColoredGrid gridPanel = new ColoredGrid(grid);
		application.add(gridPanel, BorderLayout.CENTER);
		
		button = new JButton(REPLAY);
		button.addActionListener(new BHandler());
		application.add(button, BorderLayout.PAGE_END);
		
		application.setSize(DIM * 4, (int)(DIM * 4.4));
		application.setVisible(true);
		application.repaint();

		animate(heatmapFrames, 1);
	}



	private static void animate(List<HeatmapFrame> frames, int index) throws InterruptedException {
		button.setEnabled(false);

		while(index < frames.size()) {
			fillGrid(grid, frames.get(index));
			application.repaint();
			Thread.sleep(50);
			index++;
		}
		button.setEnabled(true);
		application.repaint();
	}
	
	static class BHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (REPLAY.equals(e.getActionCommand())) {
				new Thread(() -> {
					 try {
						 animate(heatmapFrames, 0);
					 } catch (InterruptedException e1) {
						 System.exit(0);
					 }
				}).start();
			}
		}
	};

	static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;
	static private int offset = 0;


	private static void fillGrid(Color[][] grid, HeatmapFrame heatmapFrame) {
		int pixels = grid.length * grid[0].length;
		for (int r = 0; r < grid.length; r++) {
			//System.out.println("Filling row " +r+" with ");
			for (int c = 0; c < grid[r].length; c++) {
				//grid[r][c] = interpolateColor((r * c + offset) % pixels / (double) pixels, COLD, HOT);
				grid[r][c] = interpolateColor(heatmapFrame.frameGrid[r][c] / maxHits, COLD, HOT);
				//System.out.println(String.format("Offset = % 3d [Row % 3d Col % 3d] Color = %s", offset, r, c, grid[r][c].toString()));
			}
		}
		offset += DIM;
	}
	
	private static Color interpolateColor(double ratio, Color a, Color b) {
		int ax = a.getRed();
		int ay = a.getGreen();
		int az = a.getBlue();
		int cx = ax + (int) ((b.getRed() - ax) * ratio);
		int cy = ay + (int) ((b.getGreen() - ay) * ratio);
		int cz = az + (int) ((b.getBlue() - az) * ratio);
		return new Color(cx, cy, cz);
	}

}
