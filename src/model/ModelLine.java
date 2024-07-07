package model;

import javafx.geometry.Point2D;

public class ModelLine extends Shape {
    
	private static final long serialVersionUID = 1L;
	private Point2D start;
    private Point2D end;

    public ModelLine(Point2D start, Point2D end) {
        this.start = start;
        this.end = end;
    }

    public Point2D getStart() {
        return start;
    }

    public Point2D getEnd() {
        return end;
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
