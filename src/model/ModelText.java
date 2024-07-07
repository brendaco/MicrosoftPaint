/**
 * Package: model
 * File: ModelText.java
 * Author: Jake Newton
 * Date: Oct 31, 2023
 */
package model;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 
 */
public class ModelText extends Shape {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Point2D startPoint;
	private String contentText;

	/**
	 * 
	 */
	public ModelText(Point2D startXY, String startingText) {
		this.setStartPoint(startXY);
		this.setContentText(startingText);
	}

	public Point2D getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point2D startPoint) {
		this.startPoint = startPoint;
	}

	public String getContentText() {
		return contentText;
	}

	public Point2D getTopLeft() {
		// Assuming a default font size of 12 for calculation, adjust as needed
		Text text = new Text(contentText);
		Bounds textBounds = text.getBoundsInLocal();
		double x = startPoint.getX() - textBounds.getMinX();
		double y = startPoint.getY() - textBounds.getMinY();
		return new Point2D(x, y);
	}

	public void setContentText(String contentText) {
		this.contentText = contentText;
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
