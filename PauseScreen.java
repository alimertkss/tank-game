import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.Timeline;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.KeyCode;


public class PauseScreen extends StackPane {
    private Label pauseLabel;
    private Button resumeButton;
    private Button restartButton;
    private Button exitButton;

    private static List<EnemyTank> allEnemies;
    private static List<Bullet> allBullets = new ArrayList<>();

    private boolean paused = false;

    // constructor takes three actions: what to do on resume, on restart and on exit
    public PauseScreen(Runnable onResume, Runnable onExit) {
        setPrefSize(800, 600); // size of pause screen
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // semi-transparent black background

        pauseLabel = new Label("PAUSED"); // big "PAUSED" text
        pauseLabel.setTextFill(Color.WHITE);
        Font labelFont = Font.font("Roboto", FontWeight.BOLD, 50);
        pauseLabel.setFont(labelFont);
        setAlignment(pauseLabel, Pos.TOP_CENTER);
        setMargin(pauseLabel, new Insets(100, 0, 0, 0)); // push label down a bit

        Font buttonFont = Font.font("Roboto", FontWeight.BOLD, 22);

        // buttons to resume, restart or exit the game
        resumeButton = new Button("RESUME (P)");
        restartButton = new Button("RESTART (R)");
        exitButton = new Button("EXIT (ESC)");

        // set font and size for buttons
        resumeButton.setFont(buttonFont);
        restartButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        resumeButton.setPrefWidth(180);
        resumeButton.setPrefHeight(40);
        restartButton.setPrefWidth(180);
        restartButton.setPrefHeight(40);
        exitButton.setPrefWidth(180);
        exitButton.setPrefHeight(40);

        // position buttons vertically spaced
        resumeButton.setTranslateX(0);
        resumeButton.setTranslateY(-25);
        restartButton.setTranslateX(0);
        restartButton.setTranslateY(45);
        exitButton.setTranslateX(0);
        exitButton.setTranslateY(115);

        // when resume button clicked, run onResume action and resume enemies, player, bullets
        resumeButton.setOnAction(e -> {
            onResume.run();
            for (EnemyTank enemy : allEnemies) {
                enemy.resumeChecker(Main.root, Main.primaryStage.getScene());
            }
            if (Main.player != null) {
                Main.player.resumeChecker(Main.root, Main.primaryStage.getScene());
            }
            for (Bullet bullet : allBullets) {
                bullet.resumeChecker();
            }
        });

        // restart button clears everything and restarts the game
        restartButton.setOnAction(e -> {
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

                Main.startGame();
            } catch (Exception ex) {
                ex.printStackTrace();
            }});

        // exit button runs the onExit action (usually closes the game)
        exitButton.setOnAction(e -> onExit.run());

        // listen to key presses when pause screen visible
        setOnKeyPressed(e -> {
            if (!isVisible()) return;

            if (e.getCode() == KeyCode.R) {
                restartButton.fire(); // restart game
            } else if (e.getCode() == KeyCode.ESCAPE) {
                exitButton.fire(); // exit game
            } else if (e.getCode() == KeyCode.P) {
                resumeButton.fire(); // resume game
            }
        });

        // add labels and buttons to this StackPane
        getChildren().addAll(pauseLabel, resumeButton, restartButton, exitButton);
        setVisible(false); // hidden by default
    }

    public boolean isPaused() {
        return paused;
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public void setEnemies(List<EnemyTank> eList){
        allEnemies = eList;
    }

    public void setBullets(List<Bullet> bList){
        allBullets = bList;
    }
}

