package org.udesa.unoback.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.udesa.unoback.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class Dealer {
    public List<Card> fullDeck() {
        ArrayList<Card> deck = new ArrayList<>() ;
        // Generar cartas de cada color
        deck.addAll(cardsOn("Red")) ;  // todas las rojas
        deck.addAll(cardsOn("Blue")) ;  // todas las azules
        deck.addAll(cardsOn("Green")) ;  // todas las verdes
        deck.addAll(cardsOn("Yellow")) ;  // todas las amarillas

        // Mezlcar las cartas
        Collections.shuffle(deck);
        return deck ;

    }

    private List<Card> cardsOn(String color) {
        return List.of( new WildCard(),
                new SkipCard(color),
                new ReverseCard(color),
                new Draw2Card(color),
                new NumberCard(color, 1),
                new NumberCard(color, 2),
                new NumberCard(color, 3),
                new NumberCard(color, 4),
                new NumberCard(color, 5),
                new NumberCard(color, 6)
        ) ; // aca tengo que poner una por una.
    }
}
