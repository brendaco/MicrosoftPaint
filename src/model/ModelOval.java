package model;

import javafx.geometry.Point2D;

public class ModelOval extends Shape{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Point2D center;
    private double radiusX;
    private double radiusY;

    public ModelOval(Point2D center2, double radiusX, double radiusY) {
        this.center = center2;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    public Point2D getCenter() {
        return center;
    }

    public double getRadiusX() {
        return radiusX;
    }

    public double getRadiusY() {
        return radiusY;
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
