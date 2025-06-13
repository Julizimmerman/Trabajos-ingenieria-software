package org.udesa.unoback.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.udesa.unoback.model.JsonCard;
import org.udesa.unoback.model.NumberCard;
import org.udesa.unoback.service.UnoService;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UnoController.class)
class UnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean           // Inyecta un mock de UnoService en el contexto
    private UnoService unoService;

    @Test
    void newMatchShouldReturnUuid() throws Exception {
        UUID fakeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        given(unoService.newmatch(List.of("A","B"))).willReturn(fakeId);

        mockMvc.perform(post("/newmatch")
                        .param("players", "A")
                        .param("players", "B")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + fakeId + "\""));

        then(unoService).should().newmatch(List.of("A","B"));
    }

    @Test
    void playShouldReturnOkWhenValid() throws Exception {
        UUID matchId = UUID.randomUUID();
        String payload = """
            {"color":"Red","number":5,"type":"NumberCard","shout":false}
            """;

        mockMvc.perform(post("/play/{matchId}/{player}", matchId, "A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        then(unoService).should().play(eq(matchId), eq("A"), any(JsonCard.class));
    }

    @Test
    void playShouldReturnInternalServerErrorWhenException() throws Exception {
        UUID matchId = UUID.randomUUID();
        String payload = "{\"color\":\"Green\",\"number\":3,\"type\":\"NumberCard\",\"shout\":false}";

        willThrow(new RuntimeException("Carta no válida"))
                .given(unoService).play(eq(matchId), eq("A"), any(JsonCard.class));

        mockMvc.perform(post("/play/{matchId}/{player}", matchId, "A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Carta no válida"));

        then(unoService).should().play(eq(matchId), eq("A"), any(JsonCard.class));
    }

    @Test
    void drawShouldReturnOkAndInvokeService() throws Exception {
        UUID matchId = UUID.randomUUID();

        mockMvc.perform(post("/draw/{matchId}/{player}", matchId, "B"))
                .andExpect(status().isOk());

        then(unoService).should().drawcard(matchId, "B");
    }

    @Test
    void activeCardShouldReturnJson() throws Exception {
        UUID matchId = UUID.randomUUID();
        NumberCard card = new NumberCard("Blue", 7);
        given(unoService.activecard(matchId)).willReturn(card);

        mockMvc.perform(get("/activecard/{matchId}", matchId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Blue"))
                .andExpect(jsonPath("$.number").value(7))
                .andExpect(jsonPath("$.type").value("NumberCard"));
    }

    @Test
    void playerHandShouldReturnJsonArray() throws Exception {
        UUID matchId = UUID.randomUUID();
        List<NumberCard> hand = List.of(
                new NumberCard("Green", 3),
                new NumberCard("Yellow", 0)
        );
        given(unoService.playerhand(matchId)).willReturn(List.copyOf(hand));

        mockMvc.perform(get("/playerhand/{matchId}", matchId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].color").value("Green"))
                .andExpect(jsonPath("$[0].number").value(3))
                .andExpect(jsonPath("$[1].color").value("Yellow"))
                .andExpect(jsonPath("$[1].number").value(0));

        then(unoService).should().playerhand(matchId);
    }

    @Test
    void newMatchWithoutPlayersParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/newmatch")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newMatchWithSinglePlayerShouldReturnBadRequest() throws Exception {
        // Stub para que el mock de UnoService lance IllegalArgumentException
        willThrow(new IllegalArgumentException("Se necesitan al menos 2 jugadores"))
                .given(unoService).newmatch(List.of("A"));

        mockMvc.perform(post("/newmatch")
                        .param("players","A")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Se necesitan al menos 2 jugadores"));

        then(unoService).should().newmatch(List.of("A"));
    }

    @Test
    void getActiveCardWithWrongAcceptHeaderShouldReturnNotAcceptable() throws Exception {
        mockMvc.perform(get("/activecard/{matchId}", UUID.randomUUID())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void playWithMalformedJsonShouldReturnBadRequest() throws Exception {
        String badJson = "{ color: 'Red' "; // JSON roto
        mockMvc.perform(post("/play/{matchId}/{player}", UUID.randomUUID(), "A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }
}