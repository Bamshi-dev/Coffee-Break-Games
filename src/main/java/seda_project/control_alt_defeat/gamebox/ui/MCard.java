package seda_project.control_alt_defeat.gamebox.ui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

public class MCard extends Button {
    int id;
    boolean faceUp;
    boolean removed;
    int x;
    int y;
    Background cardBack;
    Background cardFront;

    /**
     * Initializes a new MCard instance with the specified coordinates and identifier. This constructor sets up the card's appearance, including its background images for the front and back faces, and configures its visual properties such as corner radius and clipping. The card is initially set to face down and not removed from play. The provided x and y coordinates can be used for positioning the card in a grid or layout, while the id is used to identify the specific card type or image to display on the front face.
     * @param x The x-coordinate for the card's position.
     * @param y The y-coordinate for the card's position.
     * @param id The identifier for the card, used to determine which image to display on the front face.
     */
    public MCard(int x, int y, int id) {
        this.id = id;
        this.x = x;
        this.y = y;

        this.faceUp = false;
        this.removed = false;

        this.getStyleClass().remove("button");
        this.getStyleClass().add("card");

        Rectangle clip = new Rectangle();
        clip.setArcWidth(50);  // Creates a 10px corner radius to match your CSS border
        clip.setArcHeight(50);
        clip.widthProperty().bind(this.widthProperty());
        clip.heightProperty().bind(this.heightProperty());
        this.setClip(clip);

        BackgroundSize backgroundSize = new BackgroundSize(
                BackgroundSize.AUTO,
                BackgroundSize.AUTO,
                false,
                false,
                false,
                true
        );

        String back = getClass().getResource("/Images/Memory/backface.png").toExternalForm();
        String front = getClass().getResource("/Images/Memory/" + id + ".png").toExternalForm();
        cardBack = new Background(new BackgroundImage(new Image(back, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        cardFront = new Background(new BackgroundImage(new Image(front, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        this.setBackground(cardBack);
    }

    public void setHeightProporties(double height, double width) {
        this.heightProperty().add(height);
        this.widthProperty().add(width);
    }

    /**
     * Sets the card to face down, updating its background to the back face image and enabling user interaction. This method is typically called when the card is not matched or needs to be reset to its initial state. The card's faceUp property is set to false, indicating that the front face is not visible, and the card is made interactive by enabling it for user clicks.
     */
    public void faceDown() {
        this.faceUp = false;
        this.setBackground(cardBack);
        this.setDisable(false);
    }

    /**
     * Returns the identifier of the card, which is used to determine which image is displayed on the front face. This method allows external components to access the card's id for comparison or matching purposes in a memory game or similar application.
     * @return The identifier of the card.
     */
    public int getid() {
        return this.id;
    }

    /**
     * Returns whether the card is currently facing up, indicating that the front face is visible. This method allows external components to check the state of the card for game logic, such as determining if two cards are a match or if a card should be flipped back down.
     * @return True if the card is facing up; false otherwise.
     */
    public boolean getFaceUp() {
        return this.faceUp;
    }

    /**
     * Sets the card to face up, updating its background to the front face image and disabling user interaction. This method is typically called when the card is matched or needs to be revealed to the player. The card's faceUp property is set to true, indicating that the front face is visible, and the card is made non-interactive by disabling it for user clicks.
     * @param b A boolean value indicating whether the card should be set to face up (true) or not (false). When set to true, the card will display its front face and be disabled for interaction.
     */
    public void setFaceUp(boolean b) {
        this.faceUp = b;
        this.setBackground(cardFront);
        this.setDisable(true);
    }
}
