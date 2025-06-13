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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UnoServiceTest {

    @Autowired
    private UnoService unoService;

    @MockBean
    private Dealer dealer;           // ← mockeamos el Dealer

    private UUID matchId;
    private final String playerA = "Julieta";
    private final String playerB = "Jack";

    @BeforeEach
    void setUp() {
        // 1) Creamos un deck de prueba: 1 descarte + 7 cartas A + 7 cartas B + 1 extra
        List<Card> testDeck = createTestDeck();

        // 2) Stub de Mockito: cada vez que UnoService llame a dealer.fullDeck()
        //    le devolvemos testDeck en lugar de barajarlo de verdad.
        when(dealer.fullDeck()).thenReturn(testDeck);

        // 3) Ahora newmatch() usará nuestro mazo controlado
        matchId = unoService.newmatch(List.of(playerA, playerB));
    }

    public static List<Card> createTestDeck() {
        List<Card> deck = new ArrayList<>();

        // 1) Carta inicial de descarte
        deck.add(new NumberCard("Blue", 0));

        // 2) Siete cartas para Julieta
        deck.add(new NumberCard("Blue", 1));
        deck.add(new SkipCard("Yellow"));
        deck.add(new ReverseCard("Green"));
        deck.add(new Draw2Card("Red"));
        deck.add(new WildCard());
        deck.add(new NumberCard("Blue", 2));
        deck.add(new WildCard());

        // 3) Siete cartas para Jack
        deck.add(new NumberCard("Red", 3));
        deck.add(new SkipCard("Green"));
        deck.add(new ReverseCard("Yellow"));
        deck.add(new Draw2Card("Blue"));
        deck.add(new WildCard());
        deck.add(new NumberCard("Red", 4));
        deck.add(new Draw2Card("Red"));

        // 4) Carta extra para robar
        deck.add(new NumberCard("Green", 7));

        return deck;  // deck.size() == 16
    }

    @Test
    void newMatchShouldReturnNonNullUuid() {
        assertNotNull(matchId, "newmatch debe devolver un UUID válido");
    }

    @Test
    void initialHandsShouldHaveSevenCards() {
        // Mano de Julieta
        assertEquals(7, unoService.playerhand(matchId).size(),
                "Cada jugador arranca con 7 cartas");
        // Jugamos una carta para rotar el turno
        unoService.play(matchId, playerA, new JsonCard("Blue", 1, "NumberCard", false));
        // Ahora mano de Jack
        assertEquals(7, unoService.playerhand(matchId).size(),
                "El segundo jugador también arranca con 7 cartas");
    }

    @Test
    void activeCardShouldBeNonNull() {
        assertNotNull(unoService.activecard(matchId),
                "Debe existir una carta activa inicial");
    }

    @Test
    void drawCardShouldIncreaseHandSize() {
        int before = unoService.playerhand(matchId).size();
        unoService.drawcard(matchId, playerA);
        assertEquals(before + 1, unoService.playerhand(matchId).size(),
                "Después de drawcard la mano debe aumentar en 1");
    }

    @Test
    void playValidCardShouldChangeTurnAndKeepHandSize() {
        Card active = unoService.activecard(matchId);
        Card playable = unoService.playerhand(matchId).stream()
                .filter(c -> c.acceptsOnTop(active))
                .findFirst().orElseThrow();

        unoService.play(matchId, playerA, playable.asJson());

        // Tras la jugada, la mano del jugador en turno (Jack) debe ser 7
        assertEquals(7, unoService.playerhand(matchId).size(),
                "Tras la jugada de Julieta, Jack debe tener 7 cartas");
        assertEquals(playable, unoService.activecard(matchId),
                "La carta activa debe ser la que jugó Julieta");
    }

    @Test
    void playOutOfTurnShouldThrowRuntimeException() {
        JsonCard wrong = new JsonCard("Blue", 5, "NumberCard", false);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                unoService.play(matchId, playerB, wrong)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("not turn"),
                "Debe indicar turno incorrecto");
    }

    @Test
    void playInvalidCardTypeShouldThrowClassNotFoundException() {
        JsonCard invalid = new JsonCard("Pink", 99, "WeirdCard", false);
        ClassNotFoundException ex = assertThrows(ClassNotFoundException.class, () ->
                unoService.play(matchId, playerA, invalid)
        );
        assertTrue(ex.getMessage().contains("WeirdCard"),
                "Debe indicar que no encontró la clase WeirdCard");
    }
}
