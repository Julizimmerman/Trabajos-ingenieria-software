package UNO;

public class NumberedCard extends ColoredCard {
    // atributos
    private final int number;

    public NumberedCard(String color, int number) {
        super(color);
        this.number = number;
    }

    public void applyEffect(Game game) {
        return ;
    }

    // metodos
    public boolean canBePlayedOver(Card cardOnTop) {
        return cardOnTop.acceptsNumberedCard(this) ;
    }

    public boolean acceptsNumberedCard(NumberedCard card) {
        return this.acceptsColor(card.getColor()) || this.number == card.number ;
    }

    public boolean acceptsSkipCard(SkipCard card) {
        return this.acceptsColor(card.getColor()) ;
    }

    public boolean acceptsDrawTwoCard(DrawTwoCard card) {
        return this.acceptsColor(card.getColor())  ;
    }

    public boolean acceptsReverseCard(ReverseCard card) {
        return this.acceptsColor(card.getColor()) ;
    }
}
