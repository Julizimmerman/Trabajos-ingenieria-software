package org.udesa.unoback.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.udesa.unoback.model.*;
import org.udesa.unoback.service.UnoService;

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
        when(dealer.fullDeck()).thenReturn(createTestDeck());
        players1 = List.of("Julieta", "Michelle");
        players2 = List.of("Julieta", "Michelle", "Emilio", "Julio");
    }

    public static List<Card> createTestDeck() {
        List<Card> deck = new ArrayList<>();
        
        String[] colors = {"Red", "Blue", "Green", "Yellow"};
        for (String color : colors) {
            for (int i = 0; i <= 9; i++) {
                deck.add(new NumberCard(color, i));
            }
            deck.add(new SkipCard(color));
            deck.add(new ReverseCard(color));
            deck.add(new Draw2Card(color));
        }
        deck.add(new WildCard());
        deck.add(new WildCard());
        deck.add(new WildCard());
        return deck;
    }

    @Test
    void newMatchShouldReturnNonNullUuidForTwoPlayers() {
        assertNotNull(unoService.newmatch(players1), "newmatch debe devolver un UUID válido para 2 jugadores");
    }

    @Test
    void initialHandsShouldHaveSevenCards() {
        UUID gameID = unoService.newmatch(players1);
        assertEquals(7, unoService.playerhand(gameID).size(),
                "Cada jugador debe arrancar con 7 cartas");
        unoService.play(gameID, players1.get(0), new JsonCard("Blue", 1, "NumberCard", false));
        assertEquals(7, unoService.playerhand(gameID).size(),
                "Segundo jugador debe mantener 7 cartas después de un play");
    }

    @Test
    void activeCardShouldBeFirstDeckCard() {
        UUID gameID = unoService.newmatch(players1);
        assertEquals(new NumberCard("Blue", 0), unoService.activecard(gameID),
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
                .findFirst().orElseThrow();
        unoService.play(gameID, players1.get(0), playable.asJson());
        assertEquals(playable, unoService.activecard(gameID),
                "activecard debe actualizarse a la carta jugada");
        assertEquals(7, unoService.playerhand(gameID).size(),
                "la mano del jugador en turno debe permanecer en 7 cartas");
    }

    @Test
    void playOutOfTurnShouldThrowRuntimeException() {
        UUID gameID = unoService.newmatch(players1);
        assertThrows(RuntimeException.class, () ->
                unoService.play(gameID, players1.get(1), new JsonCard("Blue", 1, "NumberCard", false))
        );
    }

    @Test
    void playInvalidCardTypeShouldThrowClassNotFoundException() {
        UUID gameID = unoService.newmatch(players1);
        assertThrows(ClassNotFoundException.class, () ->
                unoService.play(gameID, players1.get(0), new JsonCard("Pink", 99, "WeirdCard", false))
        );
    }

    @Test
    void multipleMatchesShouldBeIsolated() {
        UUID gameIDa = unoService.newmatch(players1);
        UUID gameIDb = unoService.newmatch(players2);
        int sizeA = unoService.playerhand(gameIDa).size();
        int sizeB = unoService.playerhand(gameIDb).size();
        unoService.drawcard(gameIDb, players2.get(0));
        assertEquals(sizeB + 1, unoService.playerhand(gameIDb).size());
        assertEquals(sizeA, unoService.playerhand(gameIDa).size());
    }
}

