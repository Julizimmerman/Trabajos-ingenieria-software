package UNO;

import java.util.*;

public class Player {
    private final String        name ;
    private       List<Card>    hand ;
    public        boolean       cantoUNO ;

    public Player(String name, List<Card> initialCards) {
        this.name = name;
        this.hand = new ArrayList<>(initialCards);
    }

    public void playCard(Card card) {
        boolean removed = hand.remove(card);
        if (!removed) {
            throw new RuntimeException("Card not in hand");
        }
        if(getAmountCards() > 1){
            cantoUNO = false ;
        }
    }

    public void drawCard(Card card) {
        this.hand.add(card);
        cantoUNO = false ;
    }

    public int getAmountCards() {
        return hand.size();
    }

    public List<Card> getHand() {
        return hand;
    }

    public void cantarUNO() {
        if(getAmountCards() == 1) {
            cantoUNO = true;
        }
    }

    public boolean hasWon() {
        return this.getAmountCards() == 0;
    }
}