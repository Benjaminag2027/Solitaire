import java.util.ArrayList;

public class Deck {
    private static ArrayList<Card> deck = new ArrayList<>();

    public static void deckInit() {
        deck.clear();

        for (Suit suit : Suit.values()) {
            for (Face face : Face.values()) {
                deck.add(new Card(suit, face));
            }
        }
    }

    public static ArrayList<Card> getDeck() {
        return deck;
    }
}