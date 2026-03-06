import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;


public class Wall {
    private ImageView imageView; // wall image
    private List<ImageView> wallList = new ArrayList<>(); // list to keep all walls

    // constructor to make a wall at x and y position
    public Wall(double x, double y) {
        Image wallImage = new Image(getClass().getResource("/assets/wall.png").toExternalForm());
        imageView = new ImageView(wallImage);

        // set size of one wall block
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);

        // set position of this wall
        imageView.setX(x);
        imageView.setY(y);
    }

    public ImageView getImageView() {
        return imageView;
    }
    public List<ImageView> getWallList() {
        return wallList;
    }
    public void setWallSize(double x, double y) {imageView.setFitWidth(x);imageView.setFitHeight(y);} // change size of this wall block

    // add many walls to the root pane to make map borders and some inside walls
    public void addWalls(Pane root) {
        for (int i = 0; i < 800; i += 10) {
            Wall upperWall = new Wall(i, 0);
            root.getChildren().add(upperWall.getImageView());
            wallList.add(upperWall.getImageView());
        }
        for (int i = 0; i < 800; i += 10) {
            Wall lowerWall = new Wall(i, 590);
            root.getChildren().add(lowerWall.getImageView());
            wallList.add(lowerWall.getImageView());
        }
        for (int i = 0; i < 600; i += 10) {
            Wall leftWall = new Wall(0, i);
            root.getChildren().add(leftWall.getImageView());
            wallList.add(leftWall.getImageView());
        }
        for (int i = 0; i < 600; i += 10) {
            Wall rightWall = new Wall(790, i);
            root.getChildren().add(rightWall.getImageView());
            wallList.add(rightWall.getImageView());
        }
        for (int i = 100; i < 350; i += 20) {
            Wall midLowerLeftWall = new Wall(i, 480);
            midLowerLeftWall.setWallSize(20, 20);
            root.getChildren().add(midLowerLeftWall.getImageView());
            wallList.add(midLowerLeftWall.getImageView());
        }
        for (int i = 450; i < 700; i += 20) {
            Wall midLowerRightWall = new Wall(i, 480);
            midLowerRightWall.setWallSize(20, 20);
            root.getChildren().add(midLowerRightWall.getImageView());
            wallList.add(midLowerRightWall.getImageView());
        }

        for (int i = 170; i < 630; i += 20) {
            Wall midWall = new Wall(i, 340);
            midWall.setWallSize(20, 20);
            root.getChildren().add(midWall.getImageView());
            wallList.add(midWall.getImageView());
        }

        for (int i = 80; i < 250; i += 20) {
            Wall midUpperLeftWall = new Wall(240, i);
            midUpperLeftWall.setWallSize(20, 20);
            root.getChildren().add(midUpperLeftWall.getImageView());
            wallList.add(midUpperLeftWall.getImageView());
        }

        for (int i = 80; i < 250; i += 20) {
            Wall midUpperRightWall = new Wall(540, i);
            midUpperRightWall.setWallSize(20, 20);
            root.getChildren().add(midUpperRightWall.getImageView());
            wallList.add(midUpperRightWall.getImageView());
        }
    }
}
