import java.awt.Color;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class Card {
    private Suit suit;
    private Face face;
    private boolean shown = false;
    private Color color; // Added color property

    private ImageIcon faceIcon;
    private static final ImageIcon backIcon = new ImageIcon("cards/back.png");

    private int x = 0;
    private int y = 0;
    
    public static final int CARD_WIDTH = 128;
    public static final int CARD_HEIGHT = 192;

    public Card(Suit suit, Face face) {
        this.suit = suit;
        this.face = face;
        
        // Set color based on suit
        if (suit == Suit.DIAMOND || suit == Suit.HEART) {
            this.color = Color.RED;
        } else {
            this.color = Color.BLACK;
        }

        String cardIconLoc = "cards/" + suit.toString().toLowerCase() + "/" + face.toString().toLowerCase() + ".png";
        this.faceIcon = new ImageIcon(cardIconLoc);
    }

    public void draw(Graphics g) {
        if (shown) {
            g.drawImage(faceIcon.getImage(), x, y, null);
        } else {
            g.drawImage(backIcon.getImage(), x, y, null);
        }
    }

    public Face getFace() { return face; }
    public Suit getSuit() { return suit; }
    public Color getColor() { return color; } // Getter for color
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public boolean isShown() { return shown; }
    
    // Helper to set both coordinates at once
    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void show() { shown = true; }
    public void hide() { shown = false; }

    public boolean isColliding(int mouseX, int mouseY) {
        return (mouseX >= this.x && mouseX <= (this.x + CARD_WIDTH)) &&
               (mouseY >= this.y && mouseY <= (this.y + CARD_HEIGHT));
    }

    @Override
    public String toString() {
        return face.toString() + " of " + suit.toString();
    }
}