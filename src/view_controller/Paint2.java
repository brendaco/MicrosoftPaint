package view_controller;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Stack;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Paint2 defines the class that creates a GUI for the paint application using
 * JavaFX. In order to run as a JavaFX application, place the following under
 * the VM arguments in run configurations. --module-path "C:\Program
 * Files\javafx-sdk-20.0.2\lib" --add-modules
 * javafx.controls,javafx.media,javafx.swing,javafx.fxml
 */
public class Paint2 extends Application {
	private VBox toolbox;
	private double lastX = -1, lastY = -1;
	private final ToggleGroup brushHardnessSelection = new ToggleGroup();
	private final RadioButton hardBrushSelection = new RadioButton();
	private final RadioButton softBrushSelection = new RadioButton();
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

	private Stack<Image> undoStack = new Stack<>();
	private Stack<Image> redoStack = new Stack<>();
	private Stage mainStage;

	private enum Tool {
		BRUSH, LINE, ERASER, RECTANGLE, OVAL, FILL, TEXT
	}

	private Tool currentTool = Tool.BRUSH;

	/**
	 * Overrides the basic JavaFX application behavior and defines the visual layout
	 * of the GUI, sets shortcuts, and determines prompts for saving and loading
	 * images at start and end of session.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		snapshotParams.setFill(Color.TRANSPARENT);
		this.mainStage = primaryStage;

		toolbox = new VBox(10);
		toolbox.setPrefWidth(100);
		setupToolbox();

		StackPane canvasHolder = new StackPane(canvas);
		canvasHolder.setStyle("-fx-border-color: gray;");
		canvasHolder.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		HBox layout = new HBox(10, toolbox, canvasHolder);
		layout.setPadding(new Insets(10));

		// Keyboard shortcuts
		KeyCodeCombination undoCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
		KeyCodeCombination redoCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);

		layout.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (undoCombination.match(keyEvent)) {
				undo();
				keyEvent.consume();
			} else if (redoCombination.match(keyEvent)) {
				redo();
				keyEvent.consume();
			}
		});

		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
		enableDrawing(canvas);

		primaryStage.setOnCloseRequest(event -> {
			Alert closeConfirmation = new Alert(Alert.AlertType.CONFIRMATION,
					"Do you want to save changes before exiting?");
			ButtonType saveButton = new ButtonType("Save");
			ButtonType discardButton = new ButtonType("Discard", ButtonBar.ButtonData.CANCEL_CLOSE);
			closeConfirmation.getButtonTypes().setAll(saveButton, discardButton, ButtonType.CANCEL);

			Optional<ButtonType> result = closeConfirmation.showAndWait();
			if (result.isPresent()) {
				if (result.get() == saveButton) {
					saveCanvasToFile(primaryStage);
				} else if (result.get() == ButtonType.CANCEL) {
					event.consume();
				}
				// If 'Discard' is chosen, or no choice is made, the application will close
				// without saving
			}
		});
	}

	/**
	 * Saves the current canvas state as a WriteableImage object onto a stack to be
	 * used in undo/redo. Results in bluriness, other way is to add action to stack
	 */
	private void saveCanvasState() {
		WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), null);
		undoStack.push(snapshot);
		redoStack.clear();
	}

	/**
	 * Implements the ability to remove an image from the canvas and add it to the
	 * stack for redo.
	 */
	private void undo() {
		if (!undoStack.isEmpty()) {
			Image lastState = undoStack.pop();
			redoStack.push(canvas.snapshot(new SnapshotParameters(), null)); // Save current state before undo
			drawImageOnCanvas(lastState);
		}
	}

	/**
	 * Implements the ability to place an image back onto the canvas from the undo
	 * stack.
	 */
	private void redo() {
		if (!redoStack.isEmpty()) {
			Image nextState = redoStack.pop();
			undoStack.push(canvas.snapshot(new SnapshotParameters(), null)); // Save current state before redo
			drawImageOnCanvas(nextState);
		}
	}

	/**
	 * Draws a given image on the current GraphicsContext
	 * 
	 * @param image The image class to be drawn on the canvas.
	 */
	private void drawImageOnCanvas(Image image) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.drawImage(image, 0, 0);
	}

	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Defines the visual GUI that exists on the left side of the Canvas and the
	 * behavior of the buttons as they change the pointer to different tools and
	 * interact with other parts of the application.
	 */
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
		toolbox.getChildren().addAll(brushButton, lineButton, eraserButton, rectangleButton, ovalButton, fillButton,
				textButton, undoButton, redoButton, colorPicker, brushSizeSlider);

		Button saveButton = new Button("Save");
		saveButton.setOnAction(event -> saveCanvasToFile(mainStage));
		Button loadButton = new Button("Load");
		loadButton.setOnAction(event -> loadImageToCanvas(mainStage));
		HBox saveLoadContainer = new HBox(5);
		saveLoadContainer.getChildren().addAll(saveButton, loadButton);
		toolbox.getChildren().add(saveLoadContainer);

		hardBrushSelection.setToggleGroup(brushHardnessSelection);
		softBrushSelection.setToggleGroup(brushHardnessSelection);
		hardBrushSelection.setText("Hard Brush");
		softBrushSelection.setText("Soft Brush");
		HBox brushModeToggle = new HBox(hardBrushSelection, softBrushSelection);
		brushModeToggle.setSpacing(5); // Adjust spacing as needed
		toolbox.getChildren().add(brushModeToggle); // Add it to your toolbox
		hardBrushSelection.setSelected(true);

	}

	/**
	 * Allows Drawing on canvas, also defines events for buttons such as saving the
	 * canvas state buttons or selecting new tools.
	 * 
	 * @param canvas The canvas object on which drawing will be enabled.
	 */
	private void enableDrawing(Canvas canvas) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		final double[] startX = new double[1]; // Start point x
		final double[] startY = new double[1]; // Start point y
		final Image[] tempSnapshot = new Image[1]; // Temporary snapshot

		canvas.setOnMousePressed(event -> {
			saveCanvasState();

			// should've been a switch but idk if they have that in java
			if (currentTool == Tool.BRUSH) {
				if (hardBrushSelection.isSelected()) {
					// Begin path for hard brush
					gc.setStroke(currentColor);
					gc.setLineWidth(currentBrushSize);
					gc.beginPath();
					gc.lineTo(event.getX(), event.getY());
				} else {
					// Prepare for soft brush drawing
					gc.beginPath();
					lastX = event.getX();
					lastY = event.getY();
					applySoftBrush(gc, lastX, lastY);
				}
			} else if (currentTool == Tool.LINE) {
				saveCanvasState();
				startX[0] = event.getX();
				startY[0] = event.getY();
				tempSnapshot[0] = canvas.snapshot(null, null);
			} else if (currentTool == Tool.RECTANGLE) {
				saveCanvasState();
				startX[0] = event.getX();
				startY[0] = event.getY();
				tempSnapshot[0] = canvas.snapshot(null, null);
			} else if (currentTool == Tool.OVAL) {
				saveCanvasState();
				startX[0] = event.getX();
				startY[0] = event.getY();
				tempSnapshot[0] = canvas.snapshot(null, null);
			} else if (currentTool == Tool.ERASER) {
				gc.setLineWidth(currentBrushSize);
				gc.fillRect(event.getX(), event.getY(), currentBrushSize, currentBrushSize);
			} else if (currentTool == Tool.FILL) {
				saveCanvasState();
				WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
				PixelReader pixelReader = image.getPixelReader();
				PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();

				int clickX = (int) event.getX();
				int clickY = (int) event.getY();
				Color targetColor = pixelReader.getColor(clickX, clickY);
				floodFill(pixelWriter, pixelReader, clickX, clickY, targetColor, currentColor);
			} else if (currentTool == Tool.TEXT) {
				TextInputDialog textDialog = new TextInputDialog();
				textDialog.setTitle("Enter Text");
				textDialog.setHeaderText("Enter the text to place on the canvas:");
				Optional<String> result = textDialog.showAndWait();
				result.ifPresent(text -> drawTextOnCanvas(text, event.getX(), event.getY()));
			}
		});

		canvas.setOnMouseDragged(event -> {
			if (currentTool == Tool.BRUSH) {
				if (hardBrushSelection.isSelected()) {
					// Continue hard brush path
					gc.lineTo(event.getX(), event.getY());
					gc.stroke();
				} else {
					// Apply soft brush on drag
					interpolateAndApplySoftBrush(gc, lastX, lastY, event.getX(), event.getY());
					lastX = event.getX();
					lastY = event.getY();
				}
			} else if (currentTool == Tool.LINE) {
				drawImageOnCanvas(tempSnapshot[0]); // Redraw the original state
				gc.setStroke(currentColor);
				gc.setLineWidth(currentBrushSize);
				gc.strokeLine(startX[0], startY[0], event.getX(), event.getY());
			} else if (currentTool == Tool.RECTANGLE) {
				drawImageOnCanvas(tempSnapshot[0]); // Redraw the original state
				double width = Math.abs(event.getX() - startX[0]);
				double height = Math.abs(event.getY() - startY[0]);
				double rectX = Math.min(event.getX(), startX[0]);
				double rectY = Math.min(event.getY(), startY[0]);
				gc.setStroke(currentColor);
				gc.setLineWidth(currentBrushSize);
				gc.strokeRect(rectX, rectY, width, height);
			} else if (currentTool == Tool.OVAL) {
				drawImageOnCanvas(tempSnapshot[0]); // Redraw the original state
				double width = Math.abs(event.getX() - startX[0]);
				double height = Math.abs(event.getY() - startY[0]);
				double ovalX = Math.min(event.getX(), startX[0]);
				double ovalY = Math.min(event.getY(), startY[0]);
				gc.setStroke(currentColor);
				gc.setLineWidth(currentBrushSize);
				gc.strokeOval(ovalX, ovalY, width, height);
			} else if (currentTool == Tool.ERASER) {
				gc.clearRect(event.getX(), event.getY(), currentBrushSize, currentBrushSize);
			}
		});

		canvas.setOnMouseReleased(event -> {
			if (currentTool == Tool.LINE) {
				gc.setStroke(currentColor);
				gc.setLineWidth(currentBrushSize);
				gc.strokeLine(startX[0], startY[0], event.getX(), event.getY());
			} else if (currentTool == Tool.RECTANGLE) {
				double width = Math.abs(event.getX() - startX[0]);
				double height = Math.abs(event.getY() - startY[0]);
				double rectX = Math.min(event.getX(), startX[0]);
				double rectY = Math.min(event.getY(), startY[0]);
				gc.setStroke(currentColor);
				gc.setLineWidth(currentBrushSize);
				gc.strokeRect(rectX, rectY, width, height);
			} else if (currentTool == Tool.OVAL) {
				double width = Math.abs(event.getX() - startX[0]);
				double height = Math.abs(event.getY() - startY[0]);
				double ovalX = Math.min(event.getX(), startX[0]);
				double ovalY = Math.min(event.getY(), startY[0]);
				gc.setStroke(currentColor);
				gc.setLineWidth(currentBrushSize);
				gc.strokeOval(ovalX, ovalY, width, height);
			}
		});
	}

	/**
	 * Allows the user to save the current canvas to their filesystem as a .png
	 * 
	 * @param primaryStage The Stage object representing the current state of the
	 *                     canvas.
	 */
	private void saveCanvasToFile(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
		File file = fileChooser.showSaveDialog(primaryStage);

		if (file != null) {
			try {
				WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
				canvas.snapshot(null, writableImage);
				BufferedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
				ImageIO.write(renderedImage, "png", file);
			} catch (IOException ex) {
				// Handle exceptions
			}
		}
	}

	/**
	 * Allows the user to select an image from their filesystem to be loaded onto
	 * the canvas.
	 * 
	 * @param primaryStage The Stage object in which the image will be loaded to.
	 */
	private void loadImageToCanvas(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Image File");
		fileChooser.getExtensionFilters()
				.addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
		File file = fileChooser.showOpenDialog(primaryStage);

		if (file != null) {
			try {
				Image image = new Image(new FileInputStream(file));
				GraphicsContext gc = canvas.getGraphicsContext2D();
				gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear current content
				gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
			} catch (FileNotFoundException ex) {
				// Handle exception
			}
		}
	}

	/**
	 * Defines the behavior for the graphics context to draw a "soft brush" effect
	 * onto the canvas at the x and y location.
	 * 
	 * @param gc the GraphicsContext effect to be drawn.
	 * @param x  x-coordinate for draw location
	 * @param y  y-coordinate for draw location
	 */
	private void applySoftBrush(GraphicsContext gc, double x, double y) {
		int radius = (int) currentBrushSize;
		for (int i = radius; i > 0; i--) {
			double innerOpacity;
			double outerOpacity = 0; // Outer opacity is always 0 for the airbrush effect

			if (radius < 4) {
				// For smaller brushes, use a less aggressive gradient and higher minimum
				// opacity
				innerOpacity = 0.5 + (0.5 * i / radius);
			} else {
				// For larger brushes, use the standard calculation
				innerOpacity = 1.0 - (double) i / radius;
			}

			RadialGradient gradient = new RadialGradient(0, 0, x, y, i, false, CycleMethod.NO_CYCLE,
					new Stop(0,
							new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(),
									innerOpacity)),
					new Stop(1, new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(),
							outerOpacity)));
			gc.setFill(gradient);
			gc.fillOval(x - i / 2, y - i / 2, i, i);
		}

	}

	/**
	 * From current x and y coordinates and the previous x and y coordinates, draws
	 * a soft brush between the two.
	 * 
	 * @param gc       The GraphicsContext object in which the brush will be drawn
	 *                 on.
	 * @param lastX    The double representing the previous x coordinate.
	 * @param lastY    The double representing the previous y coordinate.
	 * @param currentX The double representing the current x coordinate.
	 * @param currentY The double representing the current y coordinate.
	 */
	private void interpolateAndApplySoftBrush(GraphicsContext gc, double lastX, double lastY, double currentX,
			double currentY) {
		double distance = Math.sqrt(Math.pow(currentX - lastX, 2) + Math.pow(currentY - lastY, 2));
		int steps = (int) Math.max(distance / 2, 1); // The number of steps is based on the distance

		for (int i = 0; i <= steps; i++) {
			double x = lastX + (currentX - lastX) * i / steps;
			double y = lastY + (currentY - lastY) * i / steps;
			applySoftBrush(gc, x, y);
		}
	}

	/**
	 * Determines the space that needs to be filled based on the color and proceeds
	 * to fill white space in between objects.
	 * 
	 * @param pixelWriter      PixelWriter object that will do the color filling.
	 * @param pixelReader      PixelReader object that will be determining what
	 *                         needs to be filled.
	 * @param startX           The integer x-coordinate to start filling from.
	 * @param startY           The integer y-coordinate to start filling from.
	 * @param targetColor      The Color object in which the pixels will be read
	 *                         from.
	 * @param replacementColor The Color object in which the pixels will be filled
	 *                         with.
	 */
	private void floodFill(PixelWriter pixelWriter, PixelReader pixelReader, int startX, int startY, Color targetColor,
			Color replacementColor) {
		if (targetColor.equals(replacementColor)) {
			return; // No need to fill if the target and replacement colors are the same
		}

		Queue<Point> pixelQueue = new LinkedList<>();
		pixelQueue.add(new Point(startX, startY));
		WritableImage offScreenImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
		PixelWriter offScreenWriter = offScreenImage.getPixelWriter();

		// Copy the current canvas to the off-screen image
		offScreenWriter.setPixels(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), pixelReader, 0, 0);

		while (!pixelQueue.isEmpty()) {
			Point p = pixelQueue.remove();
			int x = p.x;
			int y = p.y;

			if (x < 0 || x >= canvas.getWidth() || y < 0 || y >= canvas.getHeight()) {
				continue; // Skip pixels outside the canvas
			}

			if (offScreenImage.getPixelReader().getColor(x, y).equals(targetColor)) {
				offScreenWriter.setColor(x, y, replacementColor);

				pixelQueue.add(new Point(x - 1, y));
				pixelQueue.add(new Point(x + 1, y));
				pixelQueue.add(new Point(x, y - 1));
				pixelQueue.add(new Point(x, y + 1));
			}
		}

		// Copy the off-screen image back to the main canvas
		pixelWriter.setPixels(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), offScreenImage.getPixelReader(),
				0, 0);
	}

	/**
	 * The wow-factor, using pre-selected size, creates a text node and draws it on
	 * the canvas at the pointer location.
	 * 
	 * @param text The text as a String to be written onto the canvas
	 * @param x    The x-coordinate of pointer.
	 * @param y    The y-coordinate of pointer.
	 */
	private void drawTextOnCanvas(String text, double x, double y) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(currentColor); // Use the current color for text

		// Calculate font size based on brush size
		double fontSize = 10 + (currentBrushSize - 1) * 10;
		Font font = new Font("Arial", fontSize);
		gc.setFont(font);

		// Create a Text node to measure text width
		Text tempText = new Text(text);
		tempText.setFont(font);
		double textWidth = tempText.getBoundsInLocal().getWidth();

		// Adjust the x-coordinate to center the text
		double centeredX = x - textWidth / 2;

		gc.fillText(text, centeredX, y);
	}

}