package org.udesa.unoback.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.udesa.unoback.model.JsonCard;
import org.udesa.unoback.model.Player;
import org.udesa.unoback.service.Dealer;
import org.udesa.unoback.service.UnoServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Dealer dealer;   // Para controlar el deck

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Stub del dealer para que siempre devuelva nuestro mazo de prueba
        when(dealer.fullDeck()).thenReturn(UnoServiceTest.createTestDeck());
    }

    @Test
    void playWrongTurnTest() throws Exception {
        // 1) crear un nuevo juego y obtener su UUID
        String uuid = newGame();
        // 2) pedir la mano del jugador "Julieta" (para tener JSON válido)
        List<JsonCard> hand = activeHand(uuid);

        // 3) Jack intenta jugar en turno de Julieta
        String resp = mockMvc.perform(post("/play/" + uuid + "/Jack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hand.getFirst().toString()))
                        .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 4) el mensaje debe indicar turno incorrecto
        assertEquals(Player.NotPlayersTurn + "Jack", resp);
    }

    @Test
    void newMatchShouldReturnUuid() throws Exception {
        String raw = mockMvc.perform(post("/newmatch")
                        .param("players","Julieta")
                        .param("players","Jack"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // viene entre comillas
        String uuid = mapper.readTree(raw).asText();
        assertNotNull(UUID.fromString(uuid));
    }

    @Test
    void newMatchWithoutPlayersParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/newmatch"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Falta parámetro: players")));
    }

    @Test
    void newMatchWithSinglePlayerShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/newmatch")
                        .param("players","Julieta"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Se necesitan al menos 2 jugadores"));
    }

    @Test
    void activeCardShouldReturnJson() throws Exception {
        String uuid = newGame();
        mockMvc.perform(get("/activecard/" + uuid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Blue"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.type").value("NumberCard"));
    }

    @Test
    void getActiveCardWithWrongAcceptHeaderShouldReturnNotAcceptable() throws Exception {
        String uuid = newGame();
        mockMvc.perform(get("/activecard/" + uuid)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void playerHandShouldReturnJsonArray() throws Exception {
        String uuid = newGame();
        mockMvc.perform(get("/playerhand/" + uuid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void playWithMalformedJsonShouldReturnBadRequest() throws Exception {
        String uuid = newGame();
        String badJson = "{ color: 'Red' ";
        mockMvc.perform(post("/play/" + uuid + "/Julieta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("JSON inválido")));
    }

    @Test
    void playValidCardShouldChangeActiveCardAndHand() throws Exception {
        String uuid = newGame();
        List<JsonCard> hand = activeHand(uuid);

        // elegimos la primera jugable (acepta Blue 0)
        JsonCard play = hand.stream()
                .filter(c -> c.getColor().equals("Blue") || c.getType().equals("WildCard"))
                .findFirst()
                .orElseThrow();

        // 1) jugar carta
        mockMvc.perform(post("/play/" + uuid + "/Julieta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(play)))
                .andExpect(status().isOk());

        // 2) /activecard ahora es la carta jugada
        mockMvc.perform(get("/activecard/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value(play.getColor()))
                .andExpect(jsonPath("$.number").value(play.getNumber()));

        // 3) /playerhand devuelve la mano de Jack (turno rotado) con 7 cartas
        mockMvc.perform(get("/playerhand/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void drawShouldReturnOkAndIncreaseHand() throws Exception {
        String uuid = newGame();
        // mano inicial Julieta = 7
        int before = activeHand(uuid).size();

        mockMvc.perform(post("/draw/" + uuid + "/Julieta"))
                .andExpect(status().isOk());

        // ahora Julieta roba y sigue en turno → 8 cartas
        mockMvc.perform(get("/playerhand/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(before + 1));
    }

    // ——————————————— HELPERS ———————————————

    /**
     * Simula el POST /newmatch?players=A&players=B
     * y devuelve el UUID (sin comillas) como String.
     */
    private String newGame() throws Exception {
        String resp = mockMvc.perform(post("/newmatch")
                        .param("players", "Julieta")
                        .param("players", "Jack"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // viene con comillas, así que parseamos:
        return mapper.readTree(resp).asText();
    }

    /**
     * Simula el GET /playerhand/{matchId}
     * y parsea el JSON a List<JsonCard>.
     */
    private List<JsonCard> activeHand(String uuid) throws Exception {
        String resp = mockMvc.perform(get("/playerhand/" + uuid))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readValue(resp, new TypeReference<List<JsonCard>>() { });
    }
}
