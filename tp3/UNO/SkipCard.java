package UNO;

public class SkipCard extends ColoredCard{
    // constructor
    public SkipCard(String color) {
        super(color);
    }

    // metodos
    public boolean canBePlayedOver(Card cardOnTop) {
        return cardOnTop.acceptsSkipCard(this) ;
    }

    public boolean acceptsNumberedCard(NumberedCard card) {
        return this.acceptsColor(card.getColor()) ;
    }

    public boolean acceptsSkipCard(SkipCard card) {
        return true ;
    }

    public boolean acceptsDrawTwoCard(DrawTwoCard card) {
        return this.acceptsColor(card.getColor())  ;
    }

    public boolean acceptsReverseCard(ReverseCard card) {
        return this.acceptsColor(card.getColor())  ;
    }

    public void applyEffect(Game game) {
        game.PlaySkipCard() ;
    }
}
