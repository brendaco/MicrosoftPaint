package model;

import java.io.Serializable;

/**
 * Defines the abstract class to implement serializable shapes.
 */
public abstract class Shape implements Serializable{

	private static final long serialVersionUID = 1L;
    // Common attributes for shapes can go here, e.g., color, stroke width, etc.
	
	public abstract void saveShape();
	
	public abstract void loadShape();
}
