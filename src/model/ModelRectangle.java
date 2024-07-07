package model;

import javafx.geometry.Point2D;

public class ModelRectangle extends Shape{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Point2D topLeft;
    private double width;
    private double height;

    public ModelRectangle(Point2D topLeft, double width, double height) {
        this.topLeft = topLeft;
        this.width = width;
        this.height = height;
    }

    public Point2D getTopLeft() {
        return topLeft;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

	@Override
	public void saveShape() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadShape() {
		// TODO Auto-generated method stub
		
	}
}
