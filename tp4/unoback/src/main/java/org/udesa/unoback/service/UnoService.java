package org.udesa.unoback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.udesa.unoback.model.Card;
import org.udesa.unoback.model.Match;
import org.udesa.unoback.model.Player;
import org.udesa.unoback.model.JsonCard ;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Service
public class UnoService {
    @Autowired Dealer dealer;
    private HashMap<UUID, Match> sessions = new HashMap<UUID, Match>();

    public UUID newmatch(List<String> players) {
        UUID newKey = UUID.randomUUID();
        sessions.put(newKey, Match.fullMatch(dealer.fullDeck(), players)) ;
        return newKey;
    }

    public List<Card> playerhand(UUID matchId) {
        return sessions.get(matchId).playerHand();
    }

    public Card activecard(UUID matchId) {
        return sessions.get(matchId).activeCard();
    }

    public void play(UUID matchId, String player, JsonCard jsonCard){
        sessions.get(matchId).play(player, jsonCard.asCard() );
    }

    public void drawcard(UUID matchId, String player) {
        sessions.get(matchId).drawCard(player);
    }
}
