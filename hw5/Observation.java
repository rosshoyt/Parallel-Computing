package hw5;
/*
 * Ross Hoyt/Kevin Lundeen
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents an observation from our detection device. When a location on the
 * sensor triggers, the time and the location of the detected event are recorded
 * in one of these Observation objects.
 */
public class Observation implements Serializable {


    private static final long serialVersionUID = 1L;
    public static final long EOF = Long.MAX_VALUE;  // our convention to mark EOF with a special object

    // @see https://learnopengl.com/Getting-started/Coordinate-Systems
    public static final double CLIPPING_SPACE_MAX = 1.0;
    public static final double CLIPPING_SPACE_MIN = -1.0;


    public long time; // number of milliseconds since turning on the detector device
    public double x, y; // location of the detected event on the detection grid

    public Observation(long time, double x, double y) {
        this.time = time;
        this.x = x;
        this.y = y;
    }

    public Observation() {
        this.time = EOF;
        this.x = this.y = 0.0;
    }

    public boolean isEOF() {
        return time == EOF;
    }

    @Override
    public String toString() {
        return "Observation(" + time + ", " + x + ", " + y + ")";
    }

    /**
     *
     * @param numObservations number of serialized Observations to put in generated file
     * @param numTimeSlices   upper bound (exclusive) for the random time slice each Observation is assigned
     * @param fileName        name of the file in the local directory to store the observations
     */
    public static void generateObservationFile(int numObservations, long numTimeSlices, String fileName){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            /*
            Generate requested number of Observations with random time slices between 0 to numTimeslices - 1,
            and with random X/Y coordinates being double values on the -1.0 to 1.0 clipping plane
            */
            for(int i = 0; i < numObservations; i++)
                out.writeObject(new Observation(
                      ThreadLocalRandom.current().nextLong(0, numTimeSlices),
                      ThreadLocalRandom.current().nextDouble(CLIPPING_SPACE_MIN, CLIPPING_SPACE_MAX),
                      ThreadLocalRandom.current().nextDouble(CLIPPING_SPACE_MIN, CLIPPING_SPACE_MAX)
                ));

            out.writeObject(new Observation());  // to mark EOF
            out.close();
        } catch (IOException e) {
            System.out.println("writing to " + fileName + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
