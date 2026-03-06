import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;


public class GameOverScreen {
    Pane root;

    // constructor, creates the game over screen with score and stage
    public GameOverScreen(int score, Stage stage) {
        root = new Pane();

        // set black background color
        BackgroundFill backgroundFill = new BackgroundFill(Color.BLACK, null, null);
        root.setBackground(new Background(backgroundFill));

        // big "GAME OVER" text in white
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setLayoutX(170);
        gameOverLabel.setLayoutY(160);
        gameOverLabel.setTextFill(Color.WHITE);
        gameOverLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 85));

        // show the score below game over text
        Label scoreLabel = new Label("SCORE:" + score);
        scoreLabel.setLayoutX(253);
        scoreLabel.setLayoutY(270);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Courier New",FontWeight.BOLD, 70));

        // instructions for player to restart or exit
        Label infoLabel = new Label("Press R to restart, ESC to exit!");
        infoLabel.setLayoutX(210);
        infoLabel.setLayoutY(500);
        infoLabel.setTextFill(Color.RED);
        infoLabel.setFont(new Font("Courier New", 20));

        // add all labels to the root pane
        root.getChildren().addAll(gameOverLabel, scoreLabel, infoLabel);

        // create scene with size 800x600
        Scene gameOverScene = new Scene(root, 800, 600);

        // listen to key presses on this scene
        gameOverScene.setOnKeyPressed(e -> {
            // if user presses R, restart the game
            if (e.getCode() == KeyCode.R) {
                try {
                    Main.primaryStage.setScene(null);

                    if (Main.enemySpawnTimeline != null) {
                        Main.enemySpawnTimeline.stop();
                        Main.enemySpawnTimeline = null;
                    }
                    if (Main.labelUpdater != null) {
                        Main.labelUpdater.stop();
                        Main.labelUpdater = null;
                    };

                    Bullet.setEnemies(null);
                    Bullet.setPlayer(null);

                    Main.root.getChildren().clear();
                    EnemyTank.clearAllEnemies();
                    Bullet.clearAllBullets();

                    PlayerTank.reset();

                    // start the game again fresh
                    Main.startGame();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // if user presses ESC, close the game window
            }else if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });
        // set this game over scene to the stage (window)
        stage.setScene(gameOverScene);
    }

    public Pane getRoot() {
        return root;
    }
}
