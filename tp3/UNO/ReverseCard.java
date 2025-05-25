package UNO;

public class ReverseCard extends ColoredCard{
    // constructor
    public ReverseCard(String color) {
        super(color);
    }

    // metodos
    public boolean canBePlayedOver(Card cardOnTop) {
        return cardOnTop.acceptsReverseCard(this) ;
    }
    public boolean acceptsNumberedCard(NumberedCard card) {
        return this.acceptsColor(card.getColor()) ;
    }

    public boolean acceptsSkipCard(SkipCard card) {
        return this.acceptsColor(card.getColor()) ;
    }

    public boolean acceptsDrawTwoCard(DrawTwoCard card) {
        return this.acceptsColor(card.getColor())  ;
    }

    public boolean acceptsReverseCard(ReverseCard card) {
        return true ;
    }

    public void applyEffect(Game game) {
        game.PlayReverseCard() ;
    }
}
