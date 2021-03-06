package application;
	
import java.io.IOException;
import java.util.function.UnaryOperator;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/*
 * TODO:
 * - Bound class blocks in central pane (atm they can be dragged entirely out of the window.
 * - Select specific elements (perhaps this.getIndex() on click?)
 * - Draw connections
 * PARTIAL:
 * - Save and load Model data
 * DONE:
 * - Drag and drop elements
 * - Make ClassBlock size dynamic (ie. removing unused cells) 
 */

public class Main extends Application 
{
	@Override
	public void start(Stage primaryStage) 
	{
		
		try 
		{
			
			// Set up the stage
			primaryStage.setMinHeight(600);
			primaryStage.setMinWidth(660);
			BorderPane root = new BorderPane();
			root.getStyleClass().add("root");
			GridPane tools = new GridPane();
			Pane center = new Pane();
			center.getStyleClass().add("center");
			Model data = new Model();
			
			// Define menu bar
			MenuBar menu = new MenuBar();
			Menu file = new Menu("File");
			Menu edit = new Menu("Edit");
			MenuItem save = new MenuItem("Save");
			MenuItem load = new MenuItem("Load...");
			MenuItem clear = new MenuItem("Clear elements");
			
			// Define save behavior
			EventHandler<ActionEvent> saveEvent = new EventHandler<ActionEvent> ()
			{
				@Override
				public void handle(ActionEvent e)
				{
					try
					{
						data.save("save.uml");
					}
					catch(IOException e1)
					{
						System.err.println("IO Failure.");
					}
					e.consume();
				}
			};
			
			// Define load behavior
			EventHandler<ActionEvent> loadEvent = new EventHandler<ActionEvent> ()
			{
				@Override
				public void handle(ActionEvent e)
				{
					center.getChildren().clear();
					data.clear();
					try 
					{
						data.load("save.uml");
					} 
					catch (IOException e1) 
					{
						System.err.println("IO Failure.");
					}
					refresh(data, center);
				}
			};
			
			// Define clear behavior
			EventHandler<ActionEvent> clearEvent = new EventHandler<ActionEvent> ()
			{
				@Override
				public void handle(ActionEvent e)
				{
					center.getChildren().clear();
					data.clear();
				}
			};
			
			// Attach handlers to buttons
			save.setOnAction(saveEvent);
			load.setOnAction(loadEvent);
			clear.setOnAction(clearEvent);
			
			// Add menu items to menus
			file.getItems().add(save);
			file.getItems().add(load);
			edit.getItems().add(clear);
			menu.getMenus().add(file);
			menu.getMenus().add(edit);
			
			// Define tool panel
			Button newClass = new Button("New class...");
			Button removeClass = new Button("Delete...");
			newClass.getStyleClass().add("toolbarButtons");
			removeClass.getStyleClass().add("toolbarButtons");
			tools.add(newClass,    0, 0);
			tools.add(removeClass, 0, 1);
			
			// Define New Class behavior
			EventHandler<ActionEvent> newClassEvent = new EventHandler<ActionEvent> ()
			{
				
				@Override
				public void handle(ActionEvent e)
				{
					
					// Define dialog stage
          final Stage newClassDialog = new Stage();
          newClassDialog.initModality(Modality.APPLICATION_MODAL);
          newClassDialog.initOwner(primaryStage);

          // Define interface
          GridPane newClassInterface = new GridPane();
          Text newClassTitle = new Text("New class...");
          TextField newClassName = new TextField();
          TextArea newClassAttr = new TextArea();
          TextArea newClassOper = new TextArea();
          TextArea newClassDesc = new TextArea();
          TextField newClassX = new TextField();
          TextField newClassY = new TextField();
          Button newClassSubmit = new Button("Submit");
          
          // Declare a TextFormatter filter to ensure integer values
    			UnaryOperator<Change> integers = change -> 
    			{

    				String text = change.getText();
    				return (text.matches("[0-9]*") ? change : null);

    			};
          
    			TextFormatter<String> forceIntX = new TextFormatter<>(integers);
    			TextFormatter<String> forceIntY = new TextFormatter<>(integers);
    			newClassX.setTextFormatter(forceIntX);
    			newClassY.setTextFormatter(forceIntY);
    			
    			// Define behavior on Submit
          EventHandler<ActionEvent> newClassEvent = new EventHandler<ActionEvent> ()
          {
          	
          	@Override
          	public void handle(ActionEvent e)
          	{
          		
          		// Create a new ClassModel object out of the given values
          		int i = data.addBlock(
          				new int[] { data.getTail(),
          						Integer.parseInt(newClassX.getText()),
          						Integer.parseInt(newClassY.getText()),
          						100, 100 },
          				new String[] { newClassName.getText(),
          					newClassAttr.getText(),
          					newClassOper.getText(),
          					newClassDesc.getText()} );
          		
          		// Generate ClassBlock object
          		ClassBlock newClass = data.generateClassBlock(i);
          		makeDraggable(newClass, data, i);
          		newClass.getStyleClass().add("classBlock");

          		// Place in the main window and close dialog
          		newClass.setLayoutX((double)data.getXPos(i));
          		newClass.setLayoutY((double)data.getYPos(i));
          		center.getChildren().add(newClass);
          		
          		newClassDialog.close();
          		e.consume();
          		
          	}
          };
          
          newClassSubmit.setOnAction(newClassEvent);
          
          // Place elements on Dialog
          newClassName.setPromptText("Class name...");
          newClassAttr.setPromptText("Class Attributes...");
          newClassOper.setPromptText("Class Operations...");
          newClassDesc.setPromptText("Class Description...");
          newClassX.setPromptText("Class X");
          newClassY.setPromptText("Class Y");
          
          newClassInterface.add(newClassTitle,  0, 0, 2, 1);
          newClassInterface.add(newClassName,   0, 1, 2, 1);
          newClassInterface.add(newClassAttr,   0, 2, 2, 1);
          newClassInterface.add(newClassOper,   0, 3, 2, 1);
          newClassInterface.add(newClassDesc,   0, 4, 2, 1);
          newClassInterface.add(newClassX,      0, 5);
          newClassInterface.add(newClassY,      1, 5);
          newClassInterface.add(newClassSubmit, 1, 6);
          
          // Show Dialog
          Scene dialogScene = new Scene(newClassInterface, 300, 230);
          newClassDialog.setScene(dialogScene);
          newClassDialog.show();
          e.consume();
          
				}
			};
			
			// Define Remove Class behavior
			EventHandler<ActionEvent> removeClassEvent = new EventHandler<ActionEvent> () 
			{
				@Override
				public void handle(ActionEvent e)
				{
					  
				}
			};
			
			// Attach handlers to buttons
			newClass.setOnAction(newClassEvent);
			removeClass.setOnAction(removeClassEvent);
			
			// Place elements on stage
			root.setTop(menu);
			root.setLeft(tools);
			root.setCenter(center);
			Scene scene = new Scene(root, 660, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} 
		catch(Exception e) 
		{
			
			e.printStackTrace();
			
		}
	}
	
	
	// Removes all nodes from main panel and redraws them from the Model
	private void refresh(Model data, Pane center)
	{
		center.getChildren().clear();
		
		for(int i = 0; i != data.getTail(); ++i)
		{
  		// Generate ClassBlock object
  		ClassBlock newClass = data.generateClassBlock(i);
  		makeDraggable(newClass, data, i);
  		newClass.getStyleClass().add("classBlock");

  		// Place in the main window and close dialog
  		newClass.setLayoutX((double)data.getXPos(i));
  		newClass.setLayoutY((double)data.getYPos(i));
  		center.getChildren().add(newClass);
		}
	}
	
	// Wrap nodes in this method to enable drag and drop
	private void makeDraggable(Node node, Model data, int i) 
	{
		final Delta delta = new Delta();

		// Turns the cursor into a hand over draggable elements
		node.setOnMouseEntered(me -> {
			if (!me.isPrimaryButtonDown()) 
			{
				node.getScene().setCursor(Cursor.HAND);
			}
		});

		// Turns the cursor normal after leaving a draggable element
		node.setOnMouseExited(me -> {
			if (!me.isPrimaryButtonDown()) 
			{
				node.getScene().setCursor(Cursor.DEFAULT);
			}
		});

		// Turn the cursor normal during a drag
		node.setOnMousePressed(me -> {
			if (me.isPrimaryButtonDown()) 
			{
				node.getScene().setCursor(Cursor.DEFAULT);
			}
			delta.x = me.getX();
			delta.y = me.getY();
			node.getScene().setCursor(Cursor.MOVE);
		});

		// Turn cursor normal upon release of draggable item
		node.setOnMouseReleased(me -> {
			if (!me.isPrimaryButtonDown()) 
			{
				node.getScene().setCursor(Cursor.DEFAULT);
			}
		});

		// Update node position based on dragged position
		node.setOnMouseDragged(me -> {
			
			data.setXPos(i, (int)(node.getLayoutX() + me.getX() - delta.x));
			data.setYPos(i, (int)(node.getLayoutY() + me.getY() - delta.y));
			node.setLayoutX(data.getXPos(i));
			node.setLayoutY(data.getYPos(i));

		});
	}
	
	// Used with the makeDraggable method
  private class Delta 
  {
  	
    public double x;
    public double y;
    
  }
	
  // Launch the program
	public static void main(String[] args) 
	{
		
		launch(args);
		
	}
}
