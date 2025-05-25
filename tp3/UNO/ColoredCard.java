package UNO;

public abstract class ColoredCard extends Card{
    // atributos
    public String color;

    // constructor
    public ColoredCard(String color) {
        this.color = color;
    }

    // metodos
    public boolean acceptsColor(String color) {
        return this.color.equals(color);
    }

    public String getColor() {
        return this.color;
    }

}
