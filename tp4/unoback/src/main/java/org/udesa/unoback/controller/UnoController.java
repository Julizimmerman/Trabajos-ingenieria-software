package org.udesa.unoback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.udesa.unoback.model.Card;
import org.udesa.unoback.model.JsonCard;
import org.udesa.unoback.service.UnoService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UnoController {

    @Autowired
    private UnoService unoService;

    // — 400: falta el parámetro "players"
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> onMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .badRequest()
                .body("Falta parámetro: " + ex.getParameterName());
    }

    // — 400: JSON mal formado en el @RequestBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> onParseError(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .badRequest()
                .body("JSON inválido: " + ex.getMostSpecificCause().getMessage());
    }

    // — 400: validación manual (menos de 2 jugadores)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> onIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    // — 500: cualquier otro RuntimeException (p.ej. "Carta no válida")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> onUnexpected(RuntimeException ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @PostMapping("newmatch")
    public ResponseEntity<UUID> newMatch(@RequestParam List<String> players) {
        UUID id = unoService.newmatch(players);
        return ResponseEntity.ok(id);
    }

    @PostMapping("play/{matchId}/{player}")
    public ResponseEntity<Void> play(
            @PathVariable UUID matchId,
            @PathVariable String player,
            @RequestBody JsonCard card
    ) {
        unoService.play(matchId, player, card);
        return ResponseEntity.ok().build();
    }

    @PostMapping("draw/{matchId}/{player}")
    public ResponseEntity<Void> drawCard(
            @PathVariable UUID matchId,
            @PathVariable String player
    ) {
        unoService.drawcard(matchId, player);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="activecard/{matchId}", produces=APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonCard> activeCard(@PathVariable UUID matchId) {
        JsonCard dto = unoService.activecard(matchId).asJson();
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value="playerhand/{matchId}", produces=APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JsonCard>> playerHand(@PathVariable UUID matchId) {
        List<JsonCard> dtos = unoService.playerhand(matchId).stream()
                .map(Card::asJson)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}