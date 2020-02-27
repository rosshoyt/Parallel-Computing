package hw6;

import java.io.Serializable;
import java.util.Arrays;

public class HeatMap implements Serializable, Cloneable {
	private static final long serialVersionUID = -74910217358788424L;
	private int dim = 20;
	private double low, high;
	private double[] cells;

	public HeatMap(int dim, double low, double high) {
		this.dim = dim;
		this.low = low;
		this.high = high;
		cells = new double[dim * dim];
	}
	
	public HeatMap() {
		this(20, -1.0, +1.0);
	}

	public HeatMap(double x, double y) {
		this();
		accum(x, y);
	}
	
	public int getDim() {
		return dim;
	}
	public double getLow() {
		return low;
	}
	public double getHigh() {
		return high;
	}
	
	public Object clone() {
		HeatMap copy = new HeatMap(dim, low, high);
		for (int i = 0; i < cells.length; i++)
			copy.cells[i] = cells[i];
		return copy;
	}

	private int place(double where) {
		int index = (int) ((where - low) / ((high - low) / dim));
		if (index < 0)
			return 0;
		if (index >= dim)
			return dim - 1;
		return index;
	}

	private void incrCell(int r, int c) {
		cells[r * dim + c]++;
	}

	public double getCell(int r, int c) {
		return cells[r * dim + c];
	}
	
	public void setCell(int r, int c, double value) {
		cells[r * dim + c] = value;
	}

	public static HeatMap combine(HeatMap a, HeatMap b) {
		HeatMap heatmap = new HeatMap(a.dim, a.low, a.high);
		for (int i = 0; i < heatmap.cells.length; i++)
			heatmap.cells[i] = a.cells[i] + b.cells[i];
		return heatmap;
	}

	public HeatMap accum(double x, double y) {
		incrCell(place(y), place(x));
		return this;
	}
	
	public HeatMap addWeighted(HeatMap other, double weight) {
		for (int i = 0; i < cells.length; i++)
			cells[i] += other.cells[i] * weight;
		return this;
	}
	
	public String toString() {
		return Arrays.toString(cells);
	}
}
