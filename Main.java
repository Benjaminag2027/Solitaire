import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Main extends JPanel implements MouseListener, MouseMotionListener {
    public static final int WIDTH = 960;
    public static final int HEIGHT = 1000;
    public static final int CARD_STACK_OFFSET = 33;

    // Game state variables
    public static ArrayList<Card> shuffledDeck;
    public static ArrayList<Card> drawPile;
    public static ArrayList<Card>[] board;
    public static ArrayList<Card>[] foundation;

    // Drag and drop variables
    private ArrayList<Card> selectedCards = new ArrayList<>();
    private int sourcePileIndex = -1;
    private int offsetX, offsetY;

    // Undo and New Game Logic
    private Stack<Move> moveHistory = new Stack<>();
    private ImageIcon undoButtonIcon, undoButtonPressedIcon;
    private ImageIcon newGameButtonIcon, newGameButtonPressedIcon;
    private Rectangle undoButtonBounds = new Rectangle(332, 24, 64, 64);
    private Rectangle newGameButtonBounds = new Rectangle(332, 128, 64, 64);
    private boolean isUndoPressed = false;
    private boolean isNewGamePressed = false;

    public static Random random = new Random();

    private ImageIcon background;
    private ImageIcon cardBack;
    private Rectangle stockPileBounds = new Rectangle(8, 8, 128, 192);

    public Main() {
        addMouseListener(this);
        addMouseMotionListener(this);
        
        background = new ImageIcon("background.png");
        cardBack = new ImageIcon("cards/back.png");

        // Load both normal and pressed button sprites
        undoButtonIcon = new ImageIcon("undo.png");
        undoButtonPressedIcon = new ImageIcon("undo_pressed.png");
        newGameButtonIcon = new ImageIcon("new_game.png");
        newGameButtonPressedIcon = new ImageIcon("new_game_pressed.png");

        newGame();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Solitaire");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Main panel = new Main();
        frame.add(panel);
        frame.setSize(WIDTH + 14, HEIGHT + 37);
        frame.setVisible(true);

        Timer timer = new Timer(16, e -> panel.repaint());
        timer.start();
    }
    
    public void newGame() {
        moveHistory.clear();
        selectedCards.clear();
        sourcePileIndex = -1;

        shuffle();
        boardInit();
        repaint();
    }

    public void undoMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            
            lastMove.getFromPile().addAll(lastMove.getCards());
            lastMove.getToPile().removeAll(lastMove.getCards());

            if (lastMove.wasCardFlipped()) {
                if(!lastMove.getFromPile().isEmpty()) {
                    lastMove.getFromPile().get(lastMove.getFromPile().size() - lastMove.getCards().size() - 1).hide();
                }
            }
            repaint();
        }
    }
    
    public static void shuffle() {
        Deck.deckInit(); 
        shuffledDeck = new ArrayList<>();
        drawPile = new ArrayList<>();
        board = new ArrayList[7];
        foundation = new ArrayList[4]; 

        for (int i = 0; i < 4; i++) {
            foundation[i] = new ArrayList<>();
        }

        ArrayList<Card> fullDeck = Deck.getDeck();
        Collections.shuffle(fullDeck, random);
        shuffledDeck.addAll(fullDeck);
    }

    public static void boardInit() {
        for (int i = 0; i < 7; i++) {
            board[i] = new ArrayList<>();
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j <= i; j++) {
                Card card = shuffledDeck.removeLast();
                if (j == i) {
                    card.show();
                }
                board[i].add(card);
            }
        }
    }

    public static void drawCards() {
        if (shuffledDeck.isEmpty()) {
            while (!drawPile.isEmpty()) {
                Card card = drawPile.removeLast();
                card.hide(); 
                shuffledDeck.add(card);
            }
            return;
        }

        // for (int i = 0; i < 3; i++) {
            if (!shuffledDeck.isEmpty()) {
                Card card = shuffledDeck.removeLast();
                card.show();
                drawPile.add(card);
            }
        // }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background.getImage(), 0, 0, null);

        // Draw Foundation piles
        for (int i = 0; i < 4; i++) {
            if (!foundation[i].isEmpty()) {
                int x = 416 + (i * 136);
                foundation[i].getLast().setCoords(x, 8);
                foundation[i].getLast().draw(g);
            }
        }

        // Draw the board (tableau) piles
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < board[i].size(); j++) {
                Card card = board[i].get(j);
                if (!selectedCards.contains(card)) {
                    card.setCoords((i * 136) + 8, (j * CARD_STACK_OFFSET) + 228);
                    card.draw(g);
                }
            }
        }

        // Draw the stock pile
        if (!shuffledDeck.isEmpty()) {
            g.drawImage(cardBack.getImage(), 8, 8, null);
        }

        // Draw the draw pile (waste)
        int drawPileSize = drawPile.size();
        int start = Math.max(0, drawPileSize - 3);
        for (int i = start; i < drawPileSize; i++) {
            Card card = drawPile.get(i);
            if (!selectedCards.contains(card)) {
                int x = 144 + ((i - start) * 20);
                card.setCoords(x, 8);
                card.draw(g);
            }
        }

        // Draw buttons based on their pressed state
        if (isUndoPressed) {
            g.drawImage(undoButtonPressedIcon.getImage(), undoButtonBounds.x, undoButtonBounds.y, null);
        } else {
            g.drawImage(undoButtonIcon.getImage(), undoButtonBounds.x, undoButtonBounds.y, null);
        }

        if (isNewGamePressed) {
            g.drawImage(newGameButtonPressedIcon.getImage(), newGameButtonBounds.x, newGameButtonBounds.y, null);
        } else {
            g.drawImage(newGameButtonIcon.getImage(), newGameButtonBounds.x, newGameButtonBounds.y, null);
        }

        // Always draw the selected cards last (on top of everything) at their dragged position.
        for (Card card : selectedCards) {
            card.draw(g);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // First, check if a button was pressed.
        if (undoButtonBounds.contains(e.getPoint())) {
            isUndoPressed = true;
            repaint();
            return; // A button was pressed, so don't try to select a card.
        }
        if (newGameButtonBounds.contains(e.getPoint())) {
            isNewGamePressed = true;
            repaint();
            return; // A button was pressed, so don't try to select a card.
        }

        // If no button was pressed, then check for card selection.
        selectedCards.clear();
        sourcePileIndex = -1;

        if (!drawPile.isEmpty()) {
            Card topCard = drawPile.getLast();
            if (topCard.isColliding(e.getX(), e.getY())) {
                selectedCards.add(topCard);
                sourcePileIndex = 7;
                offsetX = e.getX() - topCard.getX();
                offsetY = e.getY() - topCard.getY();
                return;
            }
        }

        for (int i = 6; i >= 0; i--) {
            for (int j = board[i].size() - 1; j >= 0; j--) {
                Card card = board[i].get(j);
                if (card.isShown() && card.isColliding(e.getX(), e.getY())) {
                    for (int k = j; k < board[i].size(); k++) {
                        selectedCards.add(board[i].get(k));
                    }
                    sourcePileIndex = i;
                    offsetX = e.getX() - selectedCards.getFirst().getX();
                    offsetY = e.getY() - selectedCards.getFirst().getY();
                    return;
                }
            }
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!selectedCards.isEmpty()) {
            Card topCard = selectedCards.getFirst();
            topCard.setX(e.getX() - offsetX);
            topCard.setY(e.getY() - offsetY);

            for (int i = 1; i < selectedCards.size(); i++) {
                Card previousCard = selectedCards.get(i - 1);
                Card currentCard = selectedCards.get(i);
                currentCard.setX(previousCard.getX());
                currentCard.setY(previousCard.getY() + CARD_STACK_OFFSET);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Check if a button action was completed.
        boolean buttonActionHandled = false;

        // Check if the "Undo" button was released on.
        if (isUndoPressed) {
            if (undoButtonBounds.contains(e.getPoint())) {
                undoMove();
            }
            buttonActionHandled = true;
        } 
        // Check if the "New Game" button was released on.
        else if (isNewGamePressed) {
            if (newGameButtonBounds.contains(e.getPoint())) {
                newGame();
            }
            buttonActionHandled = true;
        }
        
        // ALWAYS reset the pressed states after any release.
        isUndoPressed = false;
        isNewGamePressed = false;

        // If a button was involved (pressed or released), repaint and stop.
        if (buttonActionHandled) {
            repaint();
            return;
        }

        // --- Card Drop Logic (only runs if no buttons were involved) ---
        if (!selectedCards.isEmpty()) {
            boolean moveMade = false;
            Card bottomCard = selectedCards.getFirst();

            if (selectedCards.size() == 1) {
                for (int i = 0; i < 4; i++) {
                    int foundationX = 416 + (i * 136);
                    Rectangle foundationBounds = new Rectangle(foundationX, 8, Card.CARD_WIDTH, Card.CARD_HEIGHT);
                    if (foundationBounds.contains(e.getPoint())) {
                        if (isValidFoundationMove(bottomCard, foundation[i])) {
                            moveCards(foundation[i]);
                            moveMade = true;
                            break;
                        }
                    }
                }
            }
            
            if (!moveMade) {
                for (int i = 0; i < 7; i++) {
                    int boardX = (i * 136) + 8;
                    Rectangle boardBounds;
                    if (board[i].isEmpty()) {
                         boardBounds = new Rectangle(boardX, 228, Card.CARD_WIDTH, Card.CARD_HEIGHT);
                    } else {
                        Card topCard = board[i].getLast();
                        boardBounds = new Rectangle(topCard.getX(), topCard.getY(), Card.CARD_WIDTH, Card.CARD_HEIGHT);
                    }

                    if (boardBounds.contains(e.getPoint())) {
                        if (isValidBoardMove(bottomCard, board[i])) {
                            moveCards(board[i]);
                            moveMade = true;
                            break;
                        }
                    }
                }
            }

            selectedCards.clear();
            sourcePileIndex = -1;
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // This now only handles the stock pile, as buttons are handled by press/release.
        if (selectedCards.isEmpty() && stockPileBounds.contains(e.getPoint())) {
            drawCards();
            repaint();
        }
    }

    private void moveCards(ArrayList<Card> destPile) {
        boolean cardFlipped = false;
        ArrayList<Card> sourcePile = null;

        if (sourcePileIndex >= 0 && sourcePileIndex <= 6) {
            sourcePile = board[sourcePileIndex];
            if (sourcePile.size() > selectedCards.size()) {
                Card cardUnderneath = sourcePile.get(sourcePile.size() - selectedCards.size() - 1);
                if (!cardUnderneath.isShown()) {
                    cardFlipped = true;
                }
            }
        } else if (sourcePileIndex == 7) {
            sourcePile = drawPile;
        }

        if (sourcePile != null) {
            Move move = new Move(new ArrayList<>(selectedCards), sourcePile, destPile, cardFlipped);
            moveHistory.push(move);
        }
        
        destPile.addAll(selectedCards);

        if (sourcePileIndex >= 0 && sourcePileIndex <= 6) { 
            board[sourcePileIndex].removeAll(selectedCards);
            if (!board[sourcePileIndex].isEmpty()) {
                board[sourcePileIndex].getLast().show();
            }
        } else if (sourcePileIndex == 7) { 
            drawPile.removeLast();
        }
    }

    private boolean isValidFoundationMove(Card card, ArrayList<Card> foundationPile) {
        if (foundationPile.isEmpty()) {
            return card.getFace() == Face.ACE;
        } else {
            Card topCard = foundationPile.getLast();
            return card.getSuit() == topCard.getSuit() && card.getFace().ordinal() == topCard.getFace().ordinal() + 1;
        }
    }

    private boolean isValidBoardMove(Card bottomCard, ArrayList<Card> destPile) {
        if (destPile.isEmpty()) {
            return bottomCard.getFace() == Face.KING;
        } else {
            Card topCard = destPile.getLast();
            return bottomCard.getColor() != topCard.getColor() && bottomCard.getFace().ordinal() == topCard.getFace().ordinal() - 1;
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}