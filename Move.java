import java.util.ArrayList;

public class Move {
    private ArrayList<Card> cards;
    private ArrayList<Card> fromPile;
    private ArrayList<Card> toPile;
    private boolean cardFlipped;

    public Move(ArrayList<Card> cards, ArrayList<Card> fromPile, ArrayList<Card> toPile, boolean cardFlipped) {
        this.cards = cards;
        this.fromPile = fromPile;
        this.toPile = toPile;
        this.cardFlipped = cardFlipped;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public ArrayList<Card> getFromPile() {
        return fromPile;
    }

    public ArrayList<Card> getToPile() {
        return toPile;
    }

    public boolean wasCardFlipped() {
        return cardFlipped;
    }
}