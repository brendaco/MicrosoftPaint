package view_controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Shape;

public class Paint extends Application {
	private VBox toolbox;
	// private BorderPane drawingArea;
	// private Point2D startPoint;
	private Color currentColor = Color.BLACK;
	private double currentBrushSize = 2.0;
	private final SnapshotParameters snapshotParams = new SnapshotParameters();
	private final Canvas canvas = new Canvas(600, 550);
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

	private Tool currentTool = Tool.BRUSH;
	private double startX;
	private double startY;

	@Override
	public void start(Stage primaryStage) throws Exception {
		snapshotParams.setFill(Color.TRANSPARENT);

		toolbox = new VBox(10); // Spacing between toolbox items
		toolbox.setPrefWidth(100);
		setupToolbox();

		StackPane canvasHolder = new StackPane(canvas);
		canvasHolder.setStyle("-fx-border-color: gray;");
		canvasHolder.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

		HBox layout = new HBox(10, toolbox, canvasHolder);
		layout.setPadding(new Insets(10));

		// Keyboard shortcuts
		KeyCodeCombination undoCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
		KeyCodeCombination redoCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);

		layout.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (undoCombination.match(keyEvent)) {
				undo();
				keyEvent.consume(); // Consume the event to prevent it from being processed further
			} else if (redoCombination.match(keyEvent)) {
				redo();
				keyEvent.consume();
			}
		});

		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
		enableDrawing(canvas);
	}

	private void redo() {
		// TODO Auto-generated method stub
		
	}

	private void undo() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		launch(args);
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

		brushButton.setOnAction(event -> currentTool = Tool.BRUSH);
		lineButton.setOnAction(event -> currentTool = Tool.LINE);
		eraserButton.setOnAction(event -> currentTool = Tool.ERASER);
		rectangleButton.setOnAction(event -> currentTool = Tool.RECTANGLE);
		ovalButton.setOnAction(event -> currentTool = Tool.OVAL);
		fillButton.setOnAction(event -> currentTool = Tool.FILL);
		textButton.setOnAction(event -> currentTool = Tool.TEXT);

		// Color Picker
		ColorPicker colorPicker = new ColorPicker(currentColor);
		colorPicker.setOnAction(event -> currentColor = colorPicker.getValue());

		// Brush Size Slider
		Slider brushSizeSlider = new Slider(1, 10, currentBrushSize);
		brushSizeSlider.setShowTickLabels(true);
		brushSizeSlider.setShowTickMarks(true);
		brushSizeSlider.valueProperty()
				.addListener((observable, oldValue, newValue) -> currentBrushSize = newValue.doubleValue());

		// Add components to tool box
		toolbox.getChildren().addAll(brushButton, lineButton, eraserButton, rectangleButton, ovalButton, fillButton,
				textButton, undoButton, redoButton, colorPicker, brushSizeSlider);
	}
	
	private void enableDrawing(Canvas canvas) {
	    GraphicsContext gc = canvas.getGraphicsContext2D();

	    canvas.setOnMousePressed(event -> {
	    	if(currentTool == Tool.BRUSH) {
		        gc.setStroke(currentColor);
		        gc.setLineWidth(currentBrushSize);
		        gc.beginPath();
		        gc.lineTo(event.getX(), event.getY());
	    	}
			else if(currentTool == Tool.LINE) {
				startX = event.getX();
				startY = event.getY();
			}
	    	else if(currentTool == Tool.ERASER) {
	            gc.setLineWidth(currentBrushSize);
                gc.clearRect(event.getX(), event.getY(), currentBrushSize, currentBrushSize);
	    	}
	    	else if(currentTool == Tool.RECTANGLE) {
				startX = event.getX();
				startY = event.getY();
			}
	    	else if(currentTool == Tool.OVAL) {
				startX = event.getX();
				startY = event.getY();
			}
	    	else if (currentTool == Tool.FILL) {
	            fill(gc, event.getX(), event.getY(), currentColor);
	    	}
	    	else if (currentTool == Tool.TEXT) {
	    		TextInputDialog dialog = new TextInputDialog("Enter text");
	    	    dialog.setTitle("Text Input");
	    	    dialog.setHeaderText("Enter the text:");

	    	    Optional<String> result = dialog.showAndWait();

	    	    result.ifPresent(text -> {
	    	        // Render the entered text at the specified position
	    	    	gc.setFill(currentColor);
	    	        gc.fillText(text, event.getX(), event.getY());
	    	    });
	        }
	    });

	    canvas.setOnMouseDragged(event -> {
	    	if(currentTool == Tool.BRUSH) {
	    		gc.lineTo(event.getX(), event.getY());
	    		gc.stroke();
	    	}
	    	else if (currentTool == Tool.ERASER) {
                gc.clearRect(event.getX(), event.getY(), currentBrushSize, currentBrushSize);
            }
	    });
	    
	    canvas.setOnMouseReleased(event ->{
	    	if(currentTool == Tool.LINE) {
	    		gc.strokeLine(startX, startY, event.getX(), event.getY());
	    	}
	    	else if(currentTool == Tool.RECTANGLE) {
	    		gc.setStroke(currentColor);
	    		gc.strokeRect(startX, startY, event.getX()-startX, event.getY()-startY);
	    	}
	    	else if(currentTool == Tool.OVAL) {
	    		gc.setStroke(currentColor);
	    		gc.strokeOval(startX, startY, event.getX()-startX, event.getY()-startY);
	    	}
	    });
	}
	
	private void fill(GraphicsContext gc, double x, double y, Color fillColor) {
	    // Implement flood fill algorithm to fill the area with the specified color
	}
}