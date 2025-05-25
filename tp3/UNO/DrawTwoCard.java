package UNO;

public class DrawTwoCard extends ColoredCard {
    // constructor
    public DrawTwoCard(String color) {
        super(color);
    }

    // metodos
    public boolean canBePlayedOver(Card cardOnTop) {
        return cardOnTop.acceptsDrawTwoCard(this) ;
    }

    public boolean acceptsNumberedCard(NumberedCard card) {
        return this.acceptsColor(card.getColor()) ;
    }

    public boolean acceptsSkipCard(SkipCard card) {
        return this.acceptsColor(card.getColor()) ;
    }
    public boolean acceptsDrawTwoCard(DrawTwoCard card) {
        return true ;
    }
    public boolean acceptsReverseCard(ReverseCard card) {
        return this.acceptsColor(card.getColor()) ;
    }

    public void applyEffect(Game game) {
        game.PlayDrawTwoCard() ;
    }
}
