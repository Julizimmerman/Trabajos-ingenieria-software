package UNO;

import UNO.*;

public class WildCard extends Card {
    //atributos
    public String chosenColor;
    // constructor
    public WildCard() {
        super();
    }

    // metodos
    public WildCard asColor(String color){
        chosenColor = color ;
        return this ;
    }

    public boolean canBePlayedOver(Card card) {
        return true ;
    }

    public boolean acceptsNumberedCard(NumberedCard card) {
        throw new RuntimeException("This card has not been played yet.");
    }

    public boolean acceptsSkipCard(SkipCard card) {
        throw new RuntimeException("This card has not been played yet.");
    }

    public boolean acceptsReverseCard(ReverseCard card) {
        throw new RuntimeException("This card has not been played yet.");
    }

    public boolean acceptsDrawTwoCard(DrawTwoCard card) {
        throw new RuntimeException("This card has not been played yet.");
    }

    public void applyEffect(Game game) {
        game.PlayWildCard(chosenColor);
    }
}
