import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.control.Label;


public class Bullet {
    private ImageView imageView;  // bullet image
    private Stage stage;
    private double speed = 30;  // how fast the bullet moves
    private double dx;  // direction x
    private double dy;  // direction y
    private String owner;  // who shot the bullet ("player" or "enemy")

    private Timeline enemyBulletTimeline; // explosion effect for enemy bullet
    private Timeline playerBulletTimeline; // explosion effect for player bullet
    private Timeline wallControlTimeline; // explosion on wall

    private boolean hitControl = false; // stops multiple collisions

    private static List<EnemyTank> enemies; // list of all enemies
    private List<ImageView> walls; // walls to check for collisions// walls to check for collisions
    private static List<Bullet> allBullets = new ArrayList<>(); // keep track of all bullets

    private static PlayerTank player; // reference to player tank

    private static PauseScreen pauseScreen; // reference to pause screen

    public Bullet(double x, double y, double angle, Pane root, String owner, Stage stage) {
        // load bullet image
        Image bulletImage = new Image(getClass().getResource("/assets/bullet.png").toExternalForm());
        imageView = new ImageView(bulletImage);

        imageView.setFitWidth(8);
        imageView.setFitHeight(6);

        // rotate the image depending on the direction
        imageView.setRotate(angle);

        // calculate direction and position offset
        if (angle == 0) {
            x += imageView.getFitWidth();
            dx = speed;
            dy = 0;
        } else if (angle == 90) {
            y += imageView.getFitHeight();
            dx = 0;
            dy = speed;
        } else if (angle == 180) {
            x -= imageView.getFitWidth() ;
            dx = -speed;
            dy = 0;
        } else if (angle == 270) {
            y -= imageView.getFitHeight();
            dx = 0;
            dy = -speed;
        }

        imageView.setX(x);
        imageView.setY(y);

        this.owner = owner;
        this.stage = stage;

        // add this bullet to the list
        allBullets.add(this);

        // share bullets with other classes
        PlayerTank.setBullets(allBullets);
        pauseScreen.setBullets(allBullets);

        // add bullet to the screen
        root.getChildren().add(imageView);
    }

    public ImageView getImageView() {
        return imageView;
    }
    public double getSpeed() {
        return speed;
    }
    public void setWalls(List<ImageView> walls) {this.walls = walls;}
    public static void setEnemies(List<EnemyTank> eList) {
        enemies = eList;
    }
    public static void setPlayer(PlayerTank playerTank) {
        player = playerTank;
    }
    public static void setPauseScreen(PauseScreen pS) {pauseScreen = pS;}

    // this method is used to move the bullet frame by frame
    public void move(Pane root, Scene scene) {
        double previousX = imageView.getX();
        double previousY = imageView.getY();

        double nextX = previousX + dx;
        double nextY = previousY + dy;

        int steps = 10; // used for smoother collision detection

        for (int i = 1; i <= steps; i++) {
            double checkX = previousX + (dx / steps) * i;
            double checkY = previousY + (dy / steps) * i;

            imageView.setX(checkX);
            imageView.setY(checkY);

            // check if bullet hits a wall
            if (!hitControl && walls != null) {
                for (ImageView wall : walls) {
                    if (imageView.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                        hitControl = true;

                        // show small explosion on wall hit
                        ImageView explosion = new ImageView(new Image(getClass().getResource("/assets/smallExplosion.png").toExternalForm()));
                        explosion.setX(checkX);
                        explosion.setY(checkY);
                        root.getChildren().add(explosion);

                        wallControlTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> root.getChildren().remove(explosion)));
                        wallControlTimeline.setCycleCount(1);
                        wallControlTimeline.play();

                        // remove bullet from screen
                        root.getChildren().remove(imageView);
                        return;
                    }
                }
            }

            // check if bullet hits an enemy
            if (!hitControl && owner.equals("player")) {
                if (enemies != null) {
                    for (int j = 0; j < enemies.size(); j++) {
                        EnemyTank enemy = enemies.get(j);
                        if (imageView.getBoundsInParent().intersects(enemy.getImageView().getBoundsInParent())) {
                            hitControl = true;

                            // remove enemy and bullet
                            root.getChildren().remove(enemy.getImageView());
                            player.setScore();
                            enemies.remove(j);
                            root.getChildren().remove(imageView);

                            // show explosion where enemy died
                            ImageView explosion = new ImageView(new Image(getClass().getResource("/assets/explosion.png").toExternalForm()));
                            explosion.setX(enemy.getImageView().getX());
                            explosion.setY(enemy.getImageView().getY());
                            root.getChildren().add(explosion);

                            playerBulletTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> root.getChildren().remove(explosion)));
                            playerBulletTimeline.setCycleCount(1);
                            playerBulletTimeline.play();

                            return;
                        }
                    }
                }
            }

            // check if bullet hits the player
            if (!hitControl && owner.equals("enemy")) {
                if (player != null && player.isAlive())  {
                    if (imageView.getBoundsInParent().intersects(player.getImageView().getBoundsInParent())) {
                        root.getChildren().remove(player.getImageView());
                        player.setLives(); // decrease player's live
                        player.setAlive(false);
                        root.getChildren().remove(imageView);

                        hitControl = true;

                        // show explosion where player died
                        ImageView explosion = new ImageView(new Image(getClass().getResource("/assets/explosion.png").toExternalForm()));
                        explosion.setX(player.getImageView().getX());
                        explosion.setY(player.getImageView().getY());
                        root.getChildren().add(explosion);

                        enemyBulletTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                            root.getChildren().remove(explosion);
                            // check if player has no lives left
                            if (player.getLives() <= 0) {
                                stage.getScene().setRoot(new Pane());
                                GameOverScreen gos = new GameOverScreen(player.getScore(), stage);
                            }
                        }));
                        enemyBulletTimeline.setCycleCount(1);
                        enemyBulletTimeline.play();

                        // respawn player after short delay
                        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                        delay.setOnFinished(e -> {
                            if (player != null) {
                                player.respawn(root);
                            }

                            player.setAlive(true);
                        });
                        delay.play();
                        return;
                    }
                }
            }
        }
        // if no collisions, move bullet to new position
        imageView.setX(nextX);
        imageView.setY(nextY);
    }

    // pause all animations/timers
    public void stopChecker(){
        if (wallControlTimeline != null) {
            wallControlTimeline.pause();
        }
        if (playerBulletTimeline != null) {
            playerBulletTimeline.pause();
        }
        if (enemyBulletTimeline != null) {
            enemyBulletTimeline.pause();
        }
    }

    // resume animations/timers
    public void resumeChecker() {
        if (wallControlTimeline != null) {
            wallControlTimeline.play();
        }
        if (playerBulletTimeline != null) {
            playerBulletTimeline.play();
        }
        if (enemyBulletTimeline != null) {
            enemyBulletTimeline.play();
        }
    }

    // remove all bullets from screen and memory
    public static void clearAllBullets() {
        for (Bullet bullet : allBullets) {
            bullet.stopChecker();
            if (bullet.getImageView() != null && bullet.getImageView().getParent() != null) {
                ((Pane) bullet.getImageView().getParent()).getChildren().remove(bullet.getImageView());
            }
        }
        allBullets.clear();
    }
}
