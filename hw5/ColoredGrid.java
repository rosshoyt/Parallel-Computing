package hw5;
/*
 * Ross Hoyt/Kevin Lundeen
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * Base class that represents Colored grid. can be extended for additional functionality
 */
public class ColoredGrid extends JPanel {
    protected Color[][] grid;


//    public ColoredGrid(int width, int height){
//        this.width = width;
//        this.height = height;
//        grid = new Color[this.width][this.height];
//        for(int i = 0; i < width; i++)
//            for(int j = 0; j < height; j++)
//                grid[i][j] = Color.BLUE;
//
//    }
    
    public ColoredGrid(Color[][] grid) {
        this.grid = grid;
    }
    
    /**
     * Change the grid that is being displayed.
     * @param grid the new grid to display
     */
    public void setGrid(Color[][] grid) {
        this.grid = grid;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int dim = Math.min(getWidth()/getMaxCol(), getHeight()/grid.length);
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                g.setColor(grid[row][col]);
                g.fillRect(col*dim, row*dim, dim, dim);
            }
        }
    }
    
    public int getMaxCol() {
        int max = 0;
        for (Color[] row: grid)
            if (row.length > max)
                max = row.length;
        return max;
    }
}
