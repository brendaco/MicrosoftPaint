package view_controller;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import model.ModelLine;
import model.Shape;
import model.ModelOval;
import model.ModelRectangle;
import model.ModelText;

//MAKE MODELS SERIALIZABLE, MAKE FILL AN EVENT HANDLING, AND MAKE BUTTONS TO SAVE AND LOAD, 
//MAKE BRUSH AN ACTUAL THING, CHANGE BRUSH TO LINE IN CODE, MAKE BACKGROUND OF JAVAFX WHITE
//FIX SHAPES.java IN MODEL TO MAKE IT MORE ABSTRACT

public class MainPane extends BorderPane {

	private VBox toolbox;
	private BorderPane drawingArea;
	private Point2D startPoint;
	private Color currentColor = Color.BLACK;
	private double currentBrushSize = 2.0;
	private Tool currentTool = Tool.BRUSH;
	private Image brush = new Image("file:images/brush.png");
	private ImageView brushView = new ImageView(brush);
    private Image line = new Image("file:images/line.png");
	private ImageView lineView = new ImageView(line);
	private Image eraser = new Image("file:images/eraser.png");
	private ImageView eraserView = new ImageView(eraser);
	private Image rectangle = new Image("file:images/rectangle.png");
	private ImageView rectangleView = new ImageView(rectangle);
	private Image oval = new Image("file:images/oval.png");
	private ImageView ovalView = new ImageView(oval);
	private Image fill = new Image("file:images/fill.png");
	private ImageView fillView = new ImageView(fill);
	private Image text = new Image("file:images/text.png");
	private ImageView textView = new ImageView(text);
	private Image undo = new Image("file:images/undo.png");
	private ImageView undoView = new ImageView(undo);
	private Image redo = new Image("file:images/redo.png");
	private ImageView redoView = new ImageView(redo);

	private List<Shape> shapes = new ArrayList<>();
	private Stack<List<Shape>> undoStack = new Stack<>();
	private Stack<List<Shape>> redoStack = new Stack<>();

	private enum Tool {
		BRUSH, LINE, ERASER, RECTANGLE, OVAL, FILL, TEXT
	}

	public MainPane() {
		toolbox = new VBox(10); // Spacing between toolbox items
		drawingArea = new BorderPane();
		drawingArea.setStyle("-fx-background-color: white;");

		setupToolbox();
		setupDrawingMechanism();

		this.setLeft(toolbox);
		this.setCenter(drawingArea);
	}

	private void setupToolbox() {
		brushView.setFitHeight(35);
		brushView.setFitWidth(35);
		brushView.setPreserveRatio(true);
		lineView.setFitHeight(35);
		lineView.setFitWidth(35);
		lineView.setPreserveRatio(true);
		eraserView.setFitHeight(35);
		eraserView.setFitWidth(35);
		eraserView.setPreserveRatio(true);
		rectangleView.setFitHeight(35);
		rectangleView.setFitWidth(35);
		rectangleView.setPreserveRatio(true);
		ovalView.setFitHeight(35);
		ovalView.setFitWidth(35);
		ovalView.setPreserveRatio(true);
		fillView.setFitHeight(35);
		fillView.setFitWidth(35);
		fillView.setPreserveRatio(true);
		textView.setFitHeight(35);
		textView.setFitWidth(35);
		textView.setPreserveRatio(true);
		undoView.setFitHeight(35);
		undoView.setFitWidth(35);
		undoView.setPreserveRatio(true);
		redoView.setFitHeight(35);
		redoView.setFitWidth(35);
		redoView.setPreserveRatio(true);
		// Buttons for tools
		Button brushButton = new Button();
		brushButton.setGraphic(brushView);
		Button lineButton = new Button();
		lineButton.setGraphic(lineView);
		Button eraserButton = new Button();
		eraserButton.setGraphic(eraserView);
		Button rectangleButton = new Button();
		rectangleButton.setGraphic(rectangleView);
		Button ovalButton = new Button();
		ovalButton.setGraphic(ovalView);
		Button fillButton = new Button();
		fillButton.setGraphic(fillView);
		Button textButton = new Button();
		textButton.setGraphic(textView);
		Button undoButton = new Button();
		undoButton.setGraphic(undoView);
		Button redoButton = new Button();
		redoButton.setGraphic(redoView);

		// Keyboard shortcuts
		KeyCodeCombination undoCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
		KeyCodeCombination redoCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);

		this.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (undoCombination.match(keyEvent)) {
				undo();
				keyEvent.consume(); // Consume the event to prevent it from being processed further
			} else if (redoCombination.match(keyEvent)) {
				redo();
				keyEvent.consume();
			}
		});

		brushButton.setOnAction(event -> currentTool = Tool.BRUSH);
		lineButton.setOnAction(event -> currentTool = Tool.LINE);
		eraserButton.setOnAction(event -> currentTool = Tool.ERASER);
		rectangleButton.setOnAction(event -> currentTool = Tool.RECTANGLE);
		ovalButton.setOnAction(event -> currentTool = Tool.OVAL);
		fillButton.setOnAction(event -> currentTool = Tool.FILL);
		textButton.setOnAction(event -> currentTool = Tool.TEXT);
		undoButton.setOnAction(event -> undo());
		redoButton.setOnAction(event -> redo());

		// Color Picker
		ColorPicker colorPicker = new ColorPicker(currentColor);
		colorPicker.setOnAction(event -> currentColor = colorPicker.getValue());

		// Brush Size Slider
		Slider brushSizeSlider = new Slider(1, 10, currentBrushSize);
		brushSizeSlider.setShowTickLabels(true);
		brushSizeSlider.setShowTickMarks(true);
		brushSizeSlider.valueProperty()
				.addListener((observable, oldValue, newValue) -> currentBrushSize = newValue.doubleValue());

		// Add components to toolbox
		toolbox.getChildren().addAll(brushButton, lineButton, eraserButton, rectangleButton, ovalButton, fillButton, textButton,
				undoButton, redoButton, colorPicker, brushSizeSlider);
	}

	private void setupDrawingMechanism() {
		drawingArea.setOnMousePressed(event -> {
			startPoint = new Point2D(event.getX(), event.getY());

		});

		drawingArea.setOnMouseReleased(event -> {
			Point2D endPoint = new Point2D(event.getX(), event.getY());

			undoStack.push(new ArrayList<>(shapes)); // Save current state for undo
			redoStack.clear(); // Clear redo stack when a new action is taken

			switch (currentTool) {
			case BRUSH:
				ModelLine line = new ModelLine(startPoint, endPoint);
				shapes.add(line);
				drawLine(line, Color.BLACK);
				break;
			case ERASER:
				drawLine(new ModelLine(startPoint, endPoint), Color.WHITE);
				break;
			case RECTANGLE:
				double width = Math.abs(endPoint.getX() - startPoint.getX());
				double height = Math.abs(endPoint.getY() - startPoint.getY());
				Point2D topLeft = new Point2D(Math.min(startPoint.getX(), endPoint.getX()),
						Math.min(startPoint.getY(), endPoint.getY()));
				ModelRectangle rectangle = new ModelRectangle(topLeft, width, height);
				shapes.add(rectangle);
				drawRectangle(rectangle);
				break;
			case OVAL:
				double radiusX = Math.abs(endPoint.getX() - startPoint.getX()) / 2;
				double radiusY = Math.abs(endPoint.getY() - startPoint.getY()) / 2;
				Point2D center = new Point2D((startPoint.getX() + endPoint.getX()) / 2,
						(startPoint.getY() + endPoint.getY()) / 2);
				ModelOval oval = new ModelOval(center, radiusX, radiusY);
				shapes.add(oval);
				drawOval(oval);
				break;
			case TEXT:
				Point2D textTopLeft = new Point2D(startPoint.getX(), startPoint.getY());
				ModelText newText = new ModelText(textTopLeft, "Test");
				shapes.add(newText);
				drawText(newText);
				break;
			}
		});
	}

	private void drawLine(ModelLine line, Color color) {
		Line fxLine = new Line(line.getStart().getX(), line.getStart().getY(), line.getEnd().getX(),
	            line.getEnd().getY());

	    // Create a radial gradient for the feathering effect
	    RadialGradient gradient = createSoftBrushGradient(color);

	    fxLine.setStroke(gradient);
	    fxLine.setStrokeWidth(currentBrushSize);
	    drawingArea.getChildren().add(fxLine);
	}

	private void drawRectangle(ModelRectangle rectangle) {
		javafx.scene.shape.Rectangle fxRectangle = new javafx.scene.shape.Rectangle(rectangle.getTopLeft().getX(),
				rectangle.getTopLeft().getY(), rectangle.getWidth(), rectangle.getHeight());
		fxRectangle.setFill(currentColor);
		drawingArea.getChildren().add(fxRectangle);
	}

	private void drawOval(ModelOval oval) {
		Ellipse fxEllipse = new Ellipse(oval.getCenter().getX(), oval.getCenter().getY(), oval.getRadiusX(),
				oval.getRadiusY());
		fxEllipse.setFill(currentColor);
		drawingArea.getChildren().add(fxEllipse);
	}

	private void drawText(ModelText text) {
	    Point2D textTopLeft = text.getTopLeft();
	    javafx.scene.text.Text newText = new javafx.scene.text.Text(textTopLeft.getX(), textTopLeft.getY(), text.getContentText());
	    newText.setFill(currentColor);
	    newText.setFont(new Font(10 * currentBrushSize)); // Set the font size
	    drawingArea.getChildren().add(newText);
	}

	public void saveDrawing() {
		// TODO: Change to save each Shape instead of the list of shapes.
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("drawing.dat"))) {
			out.writeObject(shapes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void loadDrawing() {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("drawing.dat"))) {
			shapes = (List<Shape>) in.readObject();
			redrawAllShapes();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void redrawAllShapes() {
		drawingArea.getChildren().clear();
		for (Shape shape : shapes) {
			if (shape instanceof ModelLine) {
				drawLine((ModelLine) shape, Color.BLACK);
			} else if (shape instanceof ModelRectangle) {
				drawRectangle((ModelRectangle) shape);
			} else if (shape instanceof ModelOval) {
				drawOval((ModelOval) shape);
			}
		}
	}

	private void undo() {
		if (!undoStack.isEmpty()) {
			redoStack.push(new ArrayList<>(shapes)); // Save current state for redo
			shapes = undoStack.pop();
			redrawAllShapes();
		}

	}

	private void redo() {
		if (!redoStack.isEmpty()) {
			undoStack.push(new ArrayList<>(shapes)); // Save current state for undo
			shapes = redoStack.pop();
			redrawAllShapes();
		}
	}
	
	private RadialGradient createSoftBrushGradient(Color primaryColor) {
        return new RadialGradient(
                0, 0,
                .5, .5,
                .5,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, primaryColor),
                new Stop(1, Color.TRANSPARENT)
        );
    }

}
