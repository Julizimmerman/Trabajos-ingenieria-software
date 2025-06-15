package org.udesa.unoback.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.udesa.unoback.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UnoServiceTest {

    @Autowired
    private UnoService unoService;

    @MockBean
    private Dealer dealer;

    private UUID matchId;
    private List<String> players1;
    private List<String> players2;

    @BeforeEach
    void setUp() {
        // siempre devolvemos nuestro mazo de prueba
        when(dealer.fullDeck()).thenReturn(createTestDeck());
        players1 = List.of("Julieta", "Michelle");
        players2 = List.of("Julieta", "Michelle", "Emilio", "Julio");
        // creamos un match para players1
        matchId = unoService.newmatch(players1);
    }

    /** Mazo de prueba: 10 números por color + un Skip, Reverse y Draw2 cada uno + 3 Wild */
    public static List<Card> createTestDeck() {
        List<Card> deck = new ArrayList<>();

        String[] colors = {"Red", "Blue", "Green", "Yellow"};
        for (String color : colors) {
            // un 0…9
            for (int i = 0; i <= 9; i++) {
                deck.add(new NumberCard(color, i));
            }
            // un Skip, Reverse y Draw2
            deck.add(new SkipCard(color));
            deck.add(new ReverseCard(color));
            deck.add(new Draw2Card(color));
        }
        // tres Wild
        deck.add(new WildCard());
        deck.add(new WildCard());
        deck.add(new WildCard());

        return deck;
    }

    @Test
    void newMatchShouldReturnNonNullUuidForTwoPlayers() {
        UUID id = unoService.newmatch(players1);
        assertNotNull(id, "newmatch debe devolver un UUID válido para 2 jugadores");
    }

    @Test
    void initialHandsShouldHaveSevenCards() {
        // mano inicial de Julieta
        assertEquals(7, unoService.playerhand(matchId).size(),
                "Cada jugador arranca con 7 cartas");

        // jugamos una carta válida (Red 1 está en la mano)
        unoService.play(matchId, players1.get(0),
                new JsonCard("Red", 1, "NumberCard", false));

        // ahora mano de Michelle (siguiente jugador)
        assertEquals(7, unoService.playerhand(matchId).size(),
                "El segundo jugador también arranca con 7 cartas");
    }

    @Test
    void activeCardShouldBeFirstDeckCard() {
        UUID gameID = unoService.newmatch(players1);
        // la primera carta del deck es Red 0 → activa inicial
        assertEquals(new NumberCard("Red", 0),
                unoService.activecard(gameID),
                "La carta activa inicial debe ser la primera del deck");
    }

    @Test
    void drawCardShouldIncreaseHandSize() {
        UUID gameID = unoService.newmatch(players1);
        int before = unoService.playerhand(gameID).size();
        unoService.drawcard(gameID, players1.get(0));
        assertEquals(before + 1, unoService.playerhand(gameID).size(),
                "drawcard debe incrementar la mano en 1");
    }

    @Test
    void playValidCardShouldUpdateActiveCardAndRotateTurn() {
        UUID gameID = unoService.newmatch(players1);

        Card active = unoService.activecard(gameID);
        Card playable = unoService.playerhand(gameID).stream()
                .filter(c -> c.acceptsOnTop(active))
                .findFirst()
                .orElseThrow();

        // Julieta juega
        unoService.play(gameID, players1.get(0), playable.asJson());

        // la activa ahora es la jugada
        assertEquals(playable, unoService.activecard(gameID),
                "activecard debe actualizarse a la carta jugada");
        // y la mano del siguiente jugador sigue en 7 cartas
        assertEquals(7, unoService.playerhand(gameID).size(),
                "la mano del jugador en turno debe permanecer en 7 cartas");
    }

    @Test
    void playOutOfTurnShouldThrowRuntimeException() {
        UUID gameID = unoService.newmatch(players1);
        assertThrows(RuntimeException.class, () ->
                unoService.play(gameID, players1.get(1),
                        new JsonCard("Red", 1, "NumberCard", false))
        );
    }

    @Test
    void playInvalidCardTypeShouldThrowClassNotFoundException() {
        UUID gameID = unoService.newmatch(players1);
        assertThrows(ClassNotFoundException.class, () ->
                unoService.play(gameID, players1.get(0),
                        new JsonCard("Pink", 99, "WeirdCard", false))
        );
    }

    @Test
    void multipleMatchesShouldBeIsolated() {
        UUID a = unoService.newmatch(players1);
        UUID b = unoService.newmatch(players2);

        int sizeA = unoService.playerhand(a).size();
        int sizeB = unoService.playerhand(b).size();

        // en b roba una carta
        unoService.drawcard(b, players2.get(0));
        assertEquals(sizeB + 1, unoService.playerhand(b).size(),
                "match B debe aumentar mano");
        // A no cambia
        assertEquals(sizeA, unoService.playerhand(a).size(),
                "match A debe permanecer igual");
    }
}
