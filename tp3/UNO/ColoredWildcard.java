package UNO;

public class ColoredWildcard extends ColoredCard {
    // atributos
    private String chosenColor = null;

    // constructor
    public ColoredWildcard(String color) {
        super(color);
        this.chosenColor = color;
    }

    // metodos
    public boolean canBePlayedOver(Card card) {
        throw new RuntimeException("This WildCard has already been played");
    }

    public boolean acceptsNumberedCard(NumberedCard card) {
        return chosenColor.equals(card.getColor()) ;
    }

    public boolean acceptsSkipCard(SkipCard card) {
        return chosenColor.equals(card.getColor()) ;
    }

    public boolean acceptsReverseCard(ReverseCard card) {
        return chosenColor.equals(card.getColor()) ;
    }

    public boolean acceptsDrawTwoCard(DrawTwoCard card) {
        return chosenColor.equals(card.getColor()) ;
    }

    public void applyEffect(Game game) {
        return;
    }

    public String getColor() {
        return chosenColor;
    }
}
