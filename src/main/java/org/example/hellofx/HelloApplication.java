package org.example.hellofx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;


public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        // Load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/hellofx/dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set window icon (make sure robot.jpg is in resources)
        Image icon = new Image(HelloApplication.class.getResourceAsStream("robot.jpg"));
        stage.getIcons().add(icon);

        stage.setTitle("Stage Demo program");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.setFullScreenExitHint("To ESCAPE PRESS q");
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("q"));

        stage.show();

//        Image icon = new Image("robot.jpg"); // insert icon that's in resources
//        stage.getIcons().add(icon);               // insert icon into stage
//        stage.setTitle("Stage Demo program");
//        stage.setWidth(800);                       // stage height & width
//        stage.setHeight(500);
//        stage.setResizable(true);                // disable resizing
//        stage.setFullScreen(true);                // set full screen
//        stage.setFullScreenExitHint("To ESCAPE PRESS q");
//        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("q"));
//
//        // add image into program and set coordinates
//        Image image = new Image("robot.jpg");
//        ImageView imageView = new ImageView(image);
//        imageView.setX(400);
//        imageView.setY(400);
//        // Try to load the image programmatically
//        Image testImage = new Image(getClass().getResourceAsStream("/trashcan.png"));
//        System.out.println("Image width: " + testImage.getWidth()); // Should NOT be -1
//
//
//        Group root = new Group();
//        Scene scene = new Scene(root, Color.GRAY);
//
//        root.getChildren().add(imageView); // insert image into program
//
//
//        stage.setScene(scene);
//        stage.show();


    }
}