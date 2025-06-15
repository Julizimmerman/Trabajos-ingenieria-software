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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean          // para controlar el deck que usa el servicio
    private Dealer dealer;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Siempre devolvemos el mismo mazo de prueba
        when(dealer.fullDeck()).thenReturn(UnoServiceTest.createTestDeck());
    }

    @Test
    void playWrongTurnTest() throws Exception {
        // 1) creamos el juego y ...
        String uuid = newGame();
        // 2) pedimos la mano de Julieta
        List<JsonCard> handJulieta = activeHand(uuid);

        // 3) Michelle (jugadora B) intenta jugar en turno de Julieta
        String resp = mockMvc.perform(post("/play/" + uuid + "/Michelle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(handJulieta.get(0))))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 4) debe indicar turno incorrecto
        assertEquals(Player.NotPlayersTurn + "Michelle", resp);
    }

    @Test
    void newMatchShouldReturnUuid() throws Exception {
        String raw = mockMvc.perform(post("/newmatch")
                        .param("players", "Julieta")
                        .param("players", "Michelle"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // viene entre comillas, parseamos
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
                        .param("players", "Julieta"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Se necesitan al menos 2 jugadores"));
    }

    @Test
    void activeCardShouldReturnJson() throws Exception {
        String uuid = newGame();

        mockMvc.perform(get("/activecard/" + uuid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Red"))     // ahora cabe esperar Red, no Blue
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
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void playWithMalformedJsonShouldReturnBadRequest() throws Exception {
        String uuid = newGame();
        String badJson = "{ color: 'Red' ";  // JSON mal formado

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

        // seleccionamos la primera carta (siempre jugable en nuestro mazo de prueba)
        JsonCard toPlay = hand.get(0);

        // 1) jugamos
        mockMvc.perform(post("/play/" + uuid + "/Julieta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(toPlay)))
                .andExpect(status().isOk());

        // 2) ahora la carta activa es la que jugamos
        mockMvc.perform(get("/activecard/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value(toPlay.getColor()))
                .andExpect(jsonPath("$.number").value(toPlay.getNumber()));

        // 3) la mano del siguiente jugador (Michelle) sigue teniendo 7 cartas
        mockMvc.perform(get("/playerhand/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void drawShouldReturnOkAndIncreaseHand() throws Exception {
        String uuid = newGame();
        int before = activeHand(uuid).size();

        mockMvc.perform(post("/draw/" + uuid + "/Julieta"))
                .andExpect(status().isOk());

        // Julieta roba y aún sigue en turno → mano +1
        mockMvc.perform(get("/playerhand/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(before + 1));
    }

    // ————— HELPERS —————

    /** Simula POST /newmatch?players=Julieta&players=Michelle */
    private String newGame() throws Exception {
        String resp = mockMvc.perform(post("/newmatch")
                        .param("players", "Julieta")
                        .param("players", "Michelle"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readTree(resp).asText();
    }

    /** Simula GET /playerhand/{matchId} */
    private List<JsonCard> activeHand(String uuid) throws Exception {
        String resp = mockMvc.perform(get("/playerhand/" + uuid))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(resp, new TypeReference<List<JsonCard>>() {});
    }
}
