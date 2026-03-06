import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;


public class Main extends Application {
    // this is the main stage
    public static Stage primaryStage;
    // this timeline keeps spawning enemy tanks every few seconds
    public static Timeline enemySpawnTimeline;
    // updates the score and lives label on screen
    public static Timeline labelUpdater;
    // this is our player's tank
    public static PlayerTank player;
    // pause menu screen that shows up when game is paused
    public static PauseScreen pauseScreen;
    // the main pane that holds everything in the game
    public static Pane root;

    public static void main(String[] args) {
        // launch the game
        launch(args);
    }

    public void start(Stage primaryStage) {
        // save the stage and start the actual game setup
        Main.primaryStage = primaryStage;
        startGame();
    }

    public static void startGame() {
        // create the main pane
        root = new Pane();

        // set background color to black
        BackgroundFill backgroundFill = new BackgroundFill(Color.BLACK, null, null);
        root.setBackground(new Background(backgroundFill));

        // create a new scene with size 800x600
        Scene scene = new Scene(root, 800, 600);

        // create the pause screen and define what happens when continue or quit is clicked
        pauseScreen = new PauseScreen(() -> {
            pauseScreen.hide(); // hide pause screen
            enemySpawnTimeline.play();  // resume enemy spawning
            labelUpdater.play();  // resume label updates
            player.setPaused(false); // unpause the player
        }, () -> {
            Platform.exit(); // exit the game
        });
        root.getChildren().add(pauseScreen);  // add pause screen to the root

        // set title and show window
        primaryStage.setTitle("Tank 2025");
        primaryStage.setScene(scene);
        primaryStage.show();

        // create and place walls on the map
        Wall wall = new Wall(0, 0);
        wall.addWalls(root);
        List<ImageView> wallList = wall.getWallList();

        // list to keep track of enemy tanks
        List<EnemyTank> enemies = new ArrayList<>();
        // create the first group of enemies
        EnemyTank.createEnemy(10, root, wallList, enemies, scene, primaryStage, pauseScreen);
        // set up timeline to keep spawning enemies every 3 seconds
        enemySpawnTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            EnemyTank.createOneEnemy(root, wallList, enemies, scene, primaryStage);
        }));
        enemySpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        enemySpawnTimeline.play();EnemyTank.setEnemyList(enemies);

        // make the enemy list available to other classes
        Bullet.setEnemies(enemies);
        PlayerTank.setEnemies(enemies);
        pauseScreen.setEnemies(enemies);

        // create the player tank and place it on the map
        player = new PlayerTank(30, 540, root, primaryStage, pauseScreen);
        Bullet.setPlayer(player);
        player.setWalls(wallList);

        // link bullet and enemy classes with pause screen
        Bullet.setPauseScreen(pauseScreen);
        EnemyTank.setPauseScreen(pauseScreen);

        // create label for lives
        Label livesLabel = new Label("Lives:  " + player.getLives());
        livesLabel.setLayoutX(25);
        livesLabel.setLayoutY(40);
        livesLabel.setTextFill(Color.WHITE);
        livesLabel.setFont(new Font("Arial", 15));

        // create label for score
        Label scoreLabel = new Label("Score: " + player.getScore());
        scoreLabel.setLayoutX(25);
        scoreLabel.setLayoutY(20);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", 15));

        // this timeline updates the lives and score labels constantly
        labelUpdater = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            livesLabel.setText("Lives:  " + player.getLives());
            scoreLabel.setText("Score: " + player.getScore());
        }));
        labelUpdater.setCycleCount(Timeline.INDEFINITE);
        labelUpdater.play();

        // add labels to the root pane
        root.getChildren().addAll(livesLabel, scoreLabel);

        // enable player movement and interaction
        player.move(scene, root);
    }
}

