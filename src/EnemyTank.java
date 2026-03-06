import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import java.util.Random;
import java.util.ArrayList;


public class EnemyTank {
    private ImageView imageView; // enemy tank image
    private Stage stage;

    private Image tankImage1; // first tank image for animation
    private Image tankImage2; // second tank image for animation
    private final double speed = 10; // movement speed

    private boolean imageControl = false; // to toggle tank images for animation

    private int angle = 0;

    // timelines control moving, shooting, and changing direction
    private Timeline movementTimeline;
    private Timeline fireTimeline;
    private Timeline directionChangeTimeline;
    private Timeline bulletMovement;

    private static PauseScreen pauseScreen; // pause screen object
    private Random random = new Random();

    private List<ImageView> walls;
    private static List<EnemyTank> allEnemies;

    // constructor: create enemy tank at x,y and add to root pane
    public EnemyTank(double x, double y, Pane root, Scene scene, Stage stage) {
        tankImage1 = new Image(getClass().getResource("/assets/whiteTank1.png").toExternalForm());
        tankImage2 = new Image(getClass().getResource("/assets/whiteTank2.png").toExternalForm());
        imageView = new ImageView(tankImage1);

        imageView.setFitHeight(30);
        imageView.setFitWidth(30);

        imageView.setX(x);
        imageView.setY(y);

        this.stage = stage;

        root.getChildren().add(imageView);
        randomMovement(root, scene);  // start moving and shooting randomly
    }

    public ImageView getImageView() {
        return imageView;
    }
    public void setWalls(List<ImageView> walls) {
        this.walls = walls;
    }
    public static void setEnemyList(List<EnemyTank> enemies) {allEnemies = enemies;}
    public static void setPauseScreen(PauseScreen pS) {pauseScreen = pS;}

    // toggle tank image to create simple animation effect
    private void controlImage() {
        if (imageControl) {
            imageView.setImage(tankImage1);
        }else {
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

    // create many enemies randomly but don't put on walls or other tanks
    public static void createEnemy(int count, Pane root, List<ImageView> walls, List<EnemyTank> enemyList, Scene scene, Stage stage, PauseScreen pauseScreen) {
        Random random = new Random();
        while (enemyList.size() < count) {
            double x = random.nextInt(800);
            double y = random.nextInt(250);

            ImageView tempTank = new ImageView();
            tempTank.setX(x);
            tempTank.setY(y);
            tempTank.setFitWidth(30);
            tempTank.setFitHeight(30);

            boolean isValid = true;

            // check not on wall
            for (ImageView wall : walls) {
                if (tempTank.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                    isValid = false;
                    break;
                }
            }

            // check not on other enemy
            if (isValid) {
                for (EnemyTank existing : enemyList) {
                    if (tempTank.getBoundsInParent().intersects(existing.getImageView().getBoundsInParent())) {
                        isValid = false;
                        break;
                    }
                }
            }

            if (isValid) {
                EnemyTank enemy = new EnemyTank(x, y, root, scene, stage);
                enemy.setWalls(walls);
                enemyList.add(enemy);
            }
        }
    }

    // create one enemy randomly if valid place
    public static void createOneEnemy(Pane root, List<ImageView> walls, List<EnemyTank> enemyList, Scene scene, Stage stage) {
        Random random = new Random();

        double x = random.nextInt(800);
        double y = random.nextInt(250);

        ImageView tempTank = new ImageView();
        tempTank.setX(x);
        tempTank.setY(y);
        tempTank.setFitWidth(30);
        tempTank.setFitHeight(30);

        boolean isValid = true;

        // check not on wall
        for (ImageView wall : walls) {
            if (tempTank.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                isValid = false;
                break;
            }
        }

        // check not on other enemy
        for (EnemyTank enemy : enemyList) {
            if (tempTank.getBoundsInParent().intersects(enemy.getImageView().getBoundsInParent())) {
                isValid = false;
                break;
            }
        }

        if (isValid) {
            EnemyTank enemy = new EnemyTank(x, y, root, scene, stage);
            enemy.setWalls(walls);
            enemyList.add(enemy);
        }
    }

    // this makes the tank move randomly and shoot sometimes
    private void randomMovement(Pane root, Scene scene) {
        randomFire(root, scene); // start shooting randomly

        // change direction every 1 second randomly
        directionChangeTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int[] angles = {0, 90, 180, 270};
            angle = angles[random.nextInt(4)];
        }));
        directionChangeTimeline.setCycleCount(Timeline.INDEFINITE);
        directionChangeTimeline.play();

        // move every 0.1 seconds
        movementTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> {
            move();
        }));
        movementTimeline.setCycleCount(Timeline.INDEFINITE);
        movementTimeline.play();
    }

    // shoot bullet randomly every 1 second
    private void randomFire(Pane root, Scene scene) {
        fireTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (root.getChildren().contains(imageView)) {
                fire(root, scene);
            }
        }));
        fireTimeline.setCycleCount(Timeline.INDEFINITE);
        fireTimeline.play();
    }

    // move tank based on current angle and speed
    private void move() {
        controlImage(); // animate tank

        double speedX = 0, speedY = 0;
        double previousX = imageView.getX();
        double previousY = imageView.getY();

        if (angle == 0) {
            speedX = speed;
        } else if (angle == 90) {
            speedY = speed;
        } else if (angle == 180) {
            speedX = -speed;
        } else if (angle == 270) {
            speedY = -speed;
        }

        double newX = previousX + speedX;
        double newY = previousY + speedY;

        if (!checkWalls(previousX, previousY, newX, newY)) {
            imageView.setRotate(angle);
            imageView.setX(newX);
            imageView.setY(newY);
        }
    }

    // shoot bullet in the direction tank faces
    private void fire(Pane root, Scene scene) {
        if (allEnemies == null || allEnemies.isEmpty()) return;

        double tankX = imageView.getX();
        double tankY = imageView.getY();
        double tankWidth = imageView.getFitWidth();
        double tankHeight = imageView.getFitHeight();

        double bulletX = tankX;
        double bulletY = tankY;

        // adjust bullet start position based on tank direction
        if (angle == 0) {
            bulletX += tankWidth;
            bulletY += tankHeight / 2 - 3;
        } else if (angle == 90) {
            bulletX += tankWidth / 2 - 3;
            bulletY += tankHeight;
        } else if (angle == 180) {
            bulletX -= 8;
            bulletY += tankHeight / 2 - 3;
        } else if (angle == 270) {
            bulletX += tankWidth / 2 - 3;
            bulletY -= 8;
        }

        Bullet bullet = new Bullet(bulletX, bulletY, angle, root, "enemy", stage);
        bullet.setWalls(walls);

        bulletMovement = new Timeline(new KeyFrame(Duration.millis(85), e -> bullet.move(root, scene)));
        bulletMovement.setCycleCount(Timeline.INDEFINITE);
        bulletMovement.play();
    }

    // pause all animations/timers
    public void stopChecker() {
        if (movementTimeline != null) {
            movementTimeline.stop();
            movementTimeline = null;
        }

        if (fireTimeline != null) {
            fireTimeline.stop();
            fireTimeline = null;
        }

        if (directionChangeTimeline != null) {
            directionChangeTimeline.stop();
            directionChangeTimeline = null;
        }

        if (bulletMovement != null) {
            bulletMovement.pause();
        }
    }

    // resume animations/timers
    public void resumeChecker(Pane root, Scene scene) {
        if (movementTimeline == null) {
            movementTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> move()));
            movementTimeline.setCycleCount(Timeline.INDEFINITE);
            movementTimeline.play();
        }
        if (fireTimeline == null) {
            fireTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (root.getChildren().contains(imageView)) {
                    fire(root, scene);
                }
            }));
            fireTimeline.setCycleCount(Timeline.INDEFINITE);
            fireTimeline.play();
        }
        if (directionChangeTimeline == null) {
            directionChangeTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                int[] angles = {0, 90, 180, 270};
                angle = angles[random.nextInt(4)];
            }));
            directionChangeTimeline.setCycleCount(Timeline.INDEFINITE);
            directionChangeTimeline.play();
        }
        if (bulletMovement != null) {
            bulletMovement.play();
        }
    }

    // remove all enemies from screen and memory
    public static void clearAllEnemies() {
        for (EnemyTank enemy : allEnemies) {
            enemy.stopChecker();
            if (enemy.getImageView() != null && enemy.getImageView().getParent() != null) {
                ((Pane) enemy.getImageView().getParent()).getChildren().remove(enemy.getImageView());
            }
        }
        allEnemies.clear();

        Bullet.setEnemies(new ArrayList<>());
    }
}
