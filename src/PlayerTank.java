import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Label;
import javafx.application.Platform;


public class PlayerTank {
    private ImageView imageView; // player tank image
    private Stage stage;

    private Image tankImage1; // first tank image for animation
    private Image tankImage2; // second tank image for animation
    private final double speed = 10; // movement speed
    private int lives = 3; // player lives
    private int score = 0; // player score

    private boolean imageControl = false; // to toggle tank images for animation

    private final double startX = 30, startY = 540; // start position
    public static Timeline movementLoop; // movement timeline loop
    private static PlayerTank playerTank;

    private boolean isAlive = true; // if player is alive or dead
    private boolean paused = false; // pause status

    private PauseScreen pauseScreen; // pause screen object

    private List<ImageView> walls; // list of walls to check collisions
    private static List<EnemyTank> allEnemies; // list of all enemy tanks
    public static List<Bullet> allBullets = new ArrayList<>(); // all bullets in game

    // constructor: create player tank at x,y and add to root pane
    public PlayerTank(double x, double y, Pane root, Stage stage, PauseScreen pauseScreen) {
        tankImage1 = new Image(getClass().getResource("/assets/yellowTank1.png").toExternalForm());
        tankImage2 = new Image(getClass().getResource("/assets/yellowTank2.png").toExternalForm());
        imageView = new ImageView(tankImage1);

        imageView.setFitHeight(30);
        imageView.setFitWidth(30);

        imageView.setX(x);
        imageView.setY(y);

        this.stage = stage;
        this.pauseScreen = pauseScreen;

        // add imageView to root if not already added
        if (imageView.getParent() == null) {
            root.getChildren().add(imageView);
        }
        imageView.setVisible(true);

        respawn(root); // set to start position
    }

    public ImageView getImageView() {
        return imageView;
    }
    public int getLives() {
        return lives;
    }
    public void setLives() {lives --;} // decrease life by one
    public int getScore() {
        return score;
    }
    public void setScore() {score ++;} // increase score by one
    public Timeline getMovementLoop() {return movementLoop;}
    public void setWalls(List<ImageView> walls) {
        this.walls = walls;
    }
    public boolean isAlive() {return isAlive;}
    public void setAlive(boolean alive) {this.isAlive = alive;}
    public void setPaused(boolean paused) {this.paused = paused;}
    public static void setEnemies(List<EnemyTank> eList) {
        allEnemies = eList;
    }
    public static void setBullets(List<Bullet> bullets) {allBullets = bullets;}

    // toggle tank image to create simple animation effect
    private void controlImage() {
        if (imageControl) {
            imageView.setImage(tankImage1);
        } else {
            imageView.setImage(tankImage2);
        }
        imageControl = !imageControl;
    }

    // check if new position hits a wall
    private boolean checkWalls(double previousX, double previousY, double nextX, double nextY) {
        imageView.setX(nextX);
        imageView.setY(nextY);
        boolean check = false;

        for (ImageView wall : walls) {
            if (imageView.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                check = true;
                break;
            }
        }

        // reset position back to old one
        imageView.setX(previousX);
        imageView.setY(previousY);

        return check;
    }

    // control tank movement with keyboard input
    public void move(Scene scene, Pane root) {
        final KeyCode[] current = {null}; // current pressed key

        // stop previous movement if any
        if (movementLoop != null) {
            movementLoop.stop();
        }

        // timeline to move tank every 50 milliseconds
        movementLoop = new Timeline(new KeyFrame(Duration.millis(50), ev -> {
            if (current[0] != null) {
                double previousX = imageView.getX();
                double previousY = imageView.getY();

                switch (current[0]) {
                    case UP:
                        double newYUp = previousY - speed;
                        if (!checkWalls(previousX, previousY, previousX, newYUp)) {
                            imageView.setY(newYUp);
                            imageView.setRotate(270);
                            controlImage();
                        }
                        break;

                    case DOWN:
                        double newYDown = previousY + speed;
                        if (!checkWalls(previousX, previousY, previousX, newYDown)) {
                            imageView.setY(newYDown);
                            imageView.setRotate(90);
                            controlImage();
                        }
                        break;

                    case LEFT:
                        double newXLeft = previousX - speed;
                        if (!checkWalls(previousX, previousY, newXLeft, previousY)) {
                            imageView.setX(newXLeft);
                            imageView.setRotate(180);
                            controlImage();
                        }
                        break;

                    case RIGHT:
                        double newXRight = previousX + speed;
                        if (!checkWalls(previousX, previousY, newXRight, previousY)) {
                            imageView.setX(newXRight);
                            imageView.setRotate(0);
                            controlImage();
                        }
                        break;

                    case P: // pause game when P pressed
                        pauseScreen.show();
                        pauseScreen.toFront();
                        stopChecker();
                        for (EnemyTank enemy : allEnemies){
                            enemy.stopChecker();
                        }
                        for (Bullet bullet : allBullets){
                            bullet.stopChecker();
                        }
                        Main.enemySpawnTimeline.stop();
                        break;
                }
            }
        }));
        movementLoop.setCycleCount(Timeline.INDEFINITE);
        movementLoop.play();

        // key pressed event to set current key or shoot bullet
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.X) {
                shoot(root, scene);
            } else {
                current[0] = e.getCode();
            }
        });

        // key released event to reset current key
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == current[0]) {
                current[0] = null;
            }
        });
    }

    // shoot bullet from tank
    public void shoot(Pane root, Scene scene) {
        double bulletSpeed = 50;

        // center of tank to start bullet
        double centerX = imageView.getX() + imageView.getFitWidth() / 2;
        double centerY = imageView.getY() + imageView.getFitHeight() / 2;

        double dx = 0;
        double dy = 0;

        double angle = imageView.getRotate();

        Bullet bullet = new Bullet(centerX, centerY, angle, root, "player", stage);

        // bullet movement timeline
        Timeline bulletMovement = new Timeline(new KeyFrame(Duration.millis(85), e -> {
            bullet.setWalls(walls);
            bullet.move(root, scene);
        }));

        bulletMovement.setCycleCount(Timeline.INDEFINITE);
        bulletMovement.play();
    }

    // reset tank to start position
    public void respawn(Pane root) {
        imageView.setX(startX);
        imageView.setY(startY);
        imageView.setRotate(0);

        if (imageView.getParent() == null) {
            root.getChildren().add(imageView);
        }
        imageView.setVisible(true);
    }

    // pause all animations/timers
    public static void stopChecker() {
        if (movementLoop != null) {
            movementLoop.stop();
            movementLoop = null;
        }
    }

    // resume animations/timers
    public void resumeChecker(Pane root, Scene scene) {
        if (movementLoop == null) {
            move(scene, root);
        }
    }

    // reset static player tank and clear bullets and enemies
    public static void reset() {
        if (playerTank != null) {
            playerTank.stopChecker();
            if (playerTank.getImageView() != null && playerTank.getImageView().getParent() != null) {
                ((Pane) playerTank.getImageView().getParent()).getChildren().remove(playerTank.getImageView());
            }
            playerTank = null;
        }

        for (Bullet b : allBullets) {
            b.stopChecker();
        }
        allBullets.clear();

        if (allEnemies != null) {
            allEnemies.clear();
        }
    }
}
