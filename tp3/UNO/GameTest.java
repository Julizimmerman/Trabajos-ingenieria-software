package UNO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    private Card red2;
    private Card red4;
    private Card red5;
    private Card blue2;
    private Card blue7;
    private Card green4;
    private Card green1;
    private Card green9;
    private Card yellow3;
    private Card yellow6;
    private Card yellow4;

    private WildCard wildcard;
    private DrawTwoCard drawRed;
    private DrawTwoCard drawBlue ;

    private SkipCard skipRed ;
    private SkipCard skipBlue ;

    private ReverseCard reverseBlue ;
    private ReverseCard reverseRed;


    @BeforeEach
    public void setUp() {
        // Cartas numéricas
        red2 = new NumberedCard("Red", 2);
        red4 = new NumberedCard("Red", 4);
        red5 = new NumberedCard("Red", 5);
        blue2 = new NumberedCard("Blue", 2);
        blue7 = new NumberedCard("Blue", 7);
        green1 = new NumberedCard("Green", 1);
        green4 = new NumberedCard("Green", 4);
        green9 = new NumberedCard("Green", 9);
        yellow3 = new NumberedCard("Yellow", 3);
        yellow6 = new NumberedCard("Yellow", 6);
        yellow4 = new NumberedCard("Yellow", 4);

        // Cartas especiales
        drawRed = new DrawTwoCard("Red");
        drawBlue = new DrawTwoCard("Blue");

        skipRed = new SkipCard("Red");
        skipBlue = new SkipCard("Blue");

        reverseBlue = new ReverseCard("Blue");
        reverseRed = new ReverseCard("Red");

        wildcard = new WildCard();
    }

    @Test
    public void TestGameStartsWithTopCard() {
        List<Card> deck = List.of(red2, red4, blue2, green4, yellow3);
        assertEquals(red2, new Game(deck, 2, "A", "B").cardOnTop());
    }


    @Test
    public void TestPlaySameColorDiffNumber() {
        List<Card> simpleDeck = List.of(red2, red4, green1, blue2, green4, yellow3, yellow6);
        assertEquals(red4,
                new Game(simpleDeck, 3, "A", "B").
                        play("A", red4)
                        .cardOnTop());
    }

    @Test
    public void TestCannotPlayOutOfTurn() {
        List<Card> deck = List.of(red5, red2, red4, blue2, green4);
        Game game = new Game(deck, 2, "A", "B");

        assertThrows(RuntimeException.class, () -> game.play("B", blue2));
    }

    @Test
    public void TestCannotPlayAnotherPlayersCard() {
        List<Card> simpleDeck = List.of(red2, green1, red4, blue2, green4);
        Game game = new Game(simpleDeck, 2, "A", "B");

        assertThrows(RuntimeException.class, () -> game.play("A", blue2));
    }

    @Test
    public void TestPlayInvalidCard() {
        List<Card> simpleDeck = List.of(red2, green1, red4, blue2, green4);
        Game game = new Game(simpleDeck, 2, "A", "B");

        assertThrows(RuntimeException.class, () -> game.play("A", green1));
    }

    @Test
    public void TestPlaySameNumberDiffColor(){
        List<Card> simpleDeck = List.of(red2, green1, red4, blue2, green4, yellow3, yellow6);
        Game game = new Game(simpleDeck, 3, "A", "B")
                .play("A", red4)
                .play("B", green4) ;

        assertEquals(game.cardOnTop(), green4);
    }

    @Test
    public void TestPlaySameCard(){
        List<Card> simpleDeck = List.of(red2, green1, red4, blue2, red4, blue7, yellow3) ;
        Game game = new Game(simpleDeck, 3, "A", "B")
                .play("A", red4)
                .play("B", red4) ;

        assertEquals(game.cardOnTop(), red4);
    }

    @Test
    public void TestPlayerPassesWhenNoValidCardEvenAfterDraw() {
        List<Card> deck = List.of(red2, blue7, blue2, red4, red5, green9, yellow3);
        Game game = new Game(deck, 2, "A", "B")
                .passOrPlayAfterDraw("A") // A no tiene cartas rojas ni con número 2, y tampoco puede jugar la que roba
                .play("B", red4); // Ahora le toca a B

        assertEquals(red4, game.cardOnTop());
    }

    @Test
    public void TestPlayWildCard() {
        List<Card> simpleDeck = List.of(red2, wildcard, red4, blue2, green4, yellow3, yellow6);
        Game game = new Game(simpleDeck, 3, "A", "B")
                .play("A", wildcard.asColor("Red"));

        assertEquals("Red", ((ColoredCard) game.cardOnTop()).getColor());
    }

    @Test
    public void TestPlayAfterWildCard(){
        List<Card> simpleDeck = List.of(red2, wildcard, blue2, green4, red4, yellow3, yellow6);
        Game game = new Game(simpleDeck, 3, "A", "B")
                .play("A", wildcard.asColor("Red"))
                .play("B", red4);

        assertEquals("Red", ((ColoredCard) game.cardOnTop()).getColor());

    }

    @Test
    public void TestSkipCardSkipsNextPlayer() {
        List<Card> deck = List.of(red2, skipRed, red4, red5, blue2, red4, green4, blue2, green1, red2, yellow6, red5, yellow6, red5);
        Game game = new Game(deck, 3, "A", "B", "C")
                .play("A", skipRed)
                .play("C", red2);

        assertEquals(red2, game.cardOnTop());
    }

    @Test
    public void TestSkipCardSkipsNextInReverse() {
        List<Card> deck = List.of(red2, reverseRed, red4, skipRed, red5, red4, red5, skipRed, yellow6, red5, yellow6, red5);

        Game game = new Game(deck, 3, "A", "B", "C")
                .play("A", reverseRed) // Reversa: A → C → B
                .play("C", skipRed)      // B debería ser salteado
                .play("A", red4);         // Vuelve a A directamente

        assertEquals(red4, game.cardOnTop());
    }

    @Test
    public void TestSkipCardSkipsOpponentInTwoPlayers() {
        List<Card> deck = List.of(red2, skipRed, red4, red5, blue2, red4, green4, blue2, green1, red2);
        Game game = new Game(deck, 3, "A", "B")
                .play("A", skipRed)  // B debería ser salteado
                .play("A", red4);

        assertEquals(red4, game.cardOnTop());
    }

    @Test
    public void TestSkipCardPreventsPlayOutOfTurn() {
        List<Card> deck = List.of(red2, skipRed, red4, red5, red4, red5, yellow6, blue2);
        Game game = new Game(deck, 3, "A", "B")
                .play("A", skipRed); // B debe ser salteado

        // B intenta jugar igual
        assertThrows(RuntimeException.class, () -> game.play("B", red5));
    }

    // Test Reverse card
    @Test
    public void TestReverseCardChangesOrder() {
        List<Card> deck = List.of(blue2, reverseBlue, red5, red2, red4, blue2, green1, red2, yellow6, blue7, yellow6);
        Game game = new Game(deck, 3, "A", "B", "C")
                .play("A", reverseBlue)  // ahora el orden es C <- B <- A
                .play("C", blue7);

        assertEquals(blue7, game.cardOnTop());
    }

    @Test
    public void TestReverseActsAsSkipInTwoPlayers() {
        List<Card> deck = List.of(blue2, reverseBlue, blue7, red4, yellow6, blue2, yellow3, red2, red4, red5);
        Game game = new Game(deck, 3, "A", "B")
                .play("A", reverseBlue) // cambia el sentido → vuelve a A
                .play("A", blue7);        // A juega de nuevo

        assertEquals(blue7, game.cardOnTop());
    }

    @Test
    public void TestReverseThenSkipInThreePlayers() {
        List<Card> deck = List.of(blue2, reverseBlue, red5, blue7, red2, green1, red2, skipBlue, yellow6, red4, yellow6);
        Game game = new Game(deck, 3, "A", "B", "C")
                .play("A", reverseBlue) // orden: A → C → B → A...
                .play("C", skipBlue)    // salta B
                .play("A", blue7);        // A juega otra vez

        assertEquals(blue7, game.cardOnTop());
    }

    @Test
    public void TestReverseThenDrawTwo() {
        List<Card> deck = List.of(blue2, reverseBlue, red2, yellow6, blue2, yellow3, red2, drawBlue, red5, red4, yellow3, red4);
        Game game = new Game(deck, 3, "A", "B", "C")
                .play("A", reverseBlue) // orden: A → C → B
                .play("C", drawBlue);    // B roba 2 y se salta

        assertEquals(5, game.players.get("B").getAmountCards()); // B tenía 3 + 2 del penalizado
        assertEquals(drawBlue, game.cardOnTop());
    }

    @Test
    public void TestDoubleReverseRestoresOriginalOrder() {
        List<Card> deck = List.of(blue2, reverseBlue, red2, blue7, blue2, yellow3, red2, reverseBlue, red5, red4, yellow3, red4);
        Game game = new Game(deck, 3, "A", "B", "C");

        game.play("A", reverseBlue); // orden: A → C → B
        game.play("C", reverseBlue); // orden: A → B → C (original)
        game.play("A", blue7);

        assertEquals(blue7, game.cardOnTop());
    }

    @Test
    public void TestPlayDrawTwoCard(){
        List<Card> drawDeck = List.of(red2, green1, red4, blue2, green4, drawBlue, yellow3, yellow6, red5);
        Game game = new Game(drawDeck, 3, "A", "B")
                .play("A", blue2)
                .play("B", drawBlue);

        assertEquals(4, game.players.get("A").getAmountCards()) ;
    }

    @Test public void TestPlayDrawTwoCardAfterDrawTwoCard(){
        List<Card> drawDeck = List.of(red2, green1, red4, blue2, drawBlue, drawRed, drawRed, red5, yellow3, yellow6, red5, green4, yellow3);
        Game game = new Game(drawDeck, 4, "A", "B")
                .play("A", red4)
                .play("B", drawRed)
                .play("B", drawRed) ;

        assertEquals(7, game.players.get("A").getAmountCards()) ;
    }

    @Test public void TestCannotPlayAfterDrawTwoCard(){
        List<Card> drawDeck = List.of(red2, green1, red4, blue2, green4, drawRed, drawRed);
        assertThrows(RuntimeException.class, () -> new Game(drawDeck, 2, "A", "B")
                .play("A", red4)
                .play("B", drawRed)
                .play("A", green4));

    }

    // testeo de turnos
    @Test
    public void TestTurnRotation() {
        List<Card> deck = List.of(red2, red4, green4, blue2, green1, red2, yellow6, red5, yellow6, red5);
        Game game = new Game(deck, 3, "A", "B", "C")
                .play("A", red4) // A
                .play("B", red2) // B
                .play("C", red5); // C

        assertEquals(red5, game.cardOnTop());
    }

    // Test cantar UNO
    @Test
    public void TestPlayerCanSayUNO() {
        List<Card> deck = List.of(yellow4, red4, red2, green4, red5, wildcard, skipRed, reverseBlue);
        Game game = new Game(deck, 2, "A", "B")
                .play("A", red4).cantarUNO("A")
                .play("B", red5)
                .play("A", red2) ;

        assertEquals(3, game.players.get("B").getAmountCards());
    }

    @Test
    public void TestPlayerWins() {
        List<Card> deck = List.of(yellow4, red4, red2, green4, red5, wildcard, skipRed, reverseBlue);
        Game game = new Game(deck, 2, "A", "B")
                .play("A", red4).cantarUNO("A")
                .play("B", red5)
                .play("A", red2) ;

        assertEquals("A", game.askForWinner());
    }
}

