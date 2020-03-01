package hw5;
/*
 * Ross Hoyt/Kevin Lundeen
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents an observation from our detection device. When a location on the
 * sensor triggers, the time and the location of the detected event are recorded
 * in one of these Observation objects.
 */
public class Observation implements Serializable {


    private static final long serialVersionUID = 1L;
    public static final long EOF = Long.MAX_VALUE;  // our convention to mark EOF with a special object

    // types of series that can be written to the file
    public enum SeriesType { FULLY_RANDOM, SWEEP_SQUARE_GRID , /*TODO*/ SWEEP_RECT_GRID;}

    // Observation fields
    public long time; // number of milliseconds since turning on the detector device
    public double x, y; // location of the detected event on the detection grid

    /**
     * Observation constructor
     * @param time long time slice that this event occurred
     * @param x    double x coordinate in the range -1.0 to 1.0 (inclusive)
     * @param y    double y coordinate in the range -1.0 to 1.0 (inclusive)
     */
    public Observation(long time, double x, double y) {
        this.time = time;
        this.x = x;
        this.y = y;
    }

    /**
     * Default Observation constructor which creates a End-of-File (EOF) Observation
     */
    public Observation() {
        this.time = EOF;
        this.x = this.y = 0.0;
    }

    /**
     * Checks if this Observation object is tagged as the EIF
     * @return true if this Observation object has been tagged as the EOF
     */
    public boolean isEOF() {
        return time == EOF;
    }

    @Override
    public String toString() {
        return "Observation(" + time + ", " + x + ", " + y + ")";
    }

    /**
     * Method that creates a .dat file in the local directory containing the requested ObservationSeriesType pattern
     * @param fileName        name of the file in the local directory to store the observations
     * @param seriesType      enum for type of series to be generated
     * @param numObservations number of serialized Observations to put in generated file (for UniformDistribution,
     *                        must be a square number to guarentee numObservations == total number written to file.
     *                        If not square number, N observations written to file is floor(sqrt(numObservations))
     * @param numTimeSlices   upper bound (exclusive) for the random time slice each Observation is assigned
     */
    public static void generateObservationFile(String fileName, SeriesType seriesType, int numObservations, int numTimeSlices){
        System.out.println("Generating and writing to file a " + seriesType.name() + " series of Observations of size " + numObservations);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            // check what observation serires type requested
            switch(seriesType) {
                case FULLY_RANDOM:
                    /*
                    Generate set of Observations with random time slices between 0 (inclusive) to numTimeslices (exclusive),
                    and with random X/Y coordinates being double values on the -1.0 to 1.0 clipping plane
                    */
                    for(int i = 0; i < numObservations; i++)
                        out.writeObject(new Observation(
                              ThreadLocalRandom.current().nextInt(0, numTimeSlices),
                              ThreadLocalRandom.current().nextDouble(HeatmapFrame.CLIPPING_SPACE_MIN, HeatmapFrame.CLIPPING_SPACE_MAX),
                              ThreadLocalRandom.current().nextDouble(HeatmapFrame.CLIPPING_SPACE_MIN, HeatmapFrame.CLIPPING_SPACE_MAX)
                        ));
                    break;
                case SWEEP_SQUARE_GRID:
                    /*
                    Generate an ordered sweep of Observations across a square grid.
                    numObservations its square root is rounded down to nearest integer to
                    ensure even distribution
                    */
                    int gridDim = (int)Math.sqrt(numObservations);
                    double sectionSize = HeatmapFrame.CLIPPING_SPACE_SIZE / gridDim; // how much of clipping plane is delegated for current observation
                    double pointOffset = sectionSize / 2; // so that each point falls in the middle of its section
                    for (int col = 0, timeCount = 0; col < gridDim; col++)
                        for(int row = 0; row < gridDim; row++, timeCount++)
                            out.writeObject(new Observation(
                                  timeCount,
                                  row * sectionSize + pointOffset + HeatmapFrame.CLIPPING_SPACE_MIN,
                                  col * sectionSize + pointOffset + HeatmapFrame.CLIPPING_SPACE_MIN
                            ));
                    break;

            }
            out.writeObject(new Observation());  // to mark EOF
            out.close();

        } catch (IOException e) {
            System.out.println("writing to " + fileName + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static List<Observation> readObservationFile(String fileName) {
        List<Observation> observations = new ArrayList<>();
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            int count = 0;
            Observation obs = (Observation) in.readObject();
            System.out.println("Reading " + fileName);
            while (!obs.isEOF()) {
                observations.add(obs);
                System.out.println(++count + ": " + obs);
                obs = (Observation) in.readObject();
            }
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("reading from " + fileName + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        return observations;
    }

}
