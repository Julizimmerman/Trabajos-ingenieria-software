package UNO;

public abstract class Card {
    // solo tiene métodos
    // métodos
    public abstract void applyEffect(Game game);

    public abstract boolean canBePlayedOver(Card card);
    public abstract boolean acceptsNumberedCard(NumberedCard card);
    public abstract boolean acceptsSkipCard(SkipCard card);
    public abstract boolean acceptsReverseCard(ReverseCard card);
    public abstract boolean acceptsDrawTwoCard(DrawTwoCard card);
}

