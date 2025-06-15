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
        List<Card> deck = new ArrayList<>();

        // Colores disponibles
        String[] colors = {"Red", "Blue", "Green", "Yellow"};

        // Para cada color: una carta 0, dos de cada 1-9, dos Skip, dos Reverse, dos Draw2
        for (String color : colors) {
            // Un 0
            deck.add(new NumberCard(color, 0));
            // Dos de cada número 1 a 9
            for (int i = 1; i <= 9; i++) {
                deck.add(new NumberCard(color, i));
                deck.add(new NumberCard(color, i));
            }
            // Dos Skip
            deck.add(new SkipCard(color));
            deck.add(new SkipCard(color));
            // Dos Reverse
            deck.add(new ReverseCard(color));
            deck.add(new ReverseCard(color));
            // Dos Draw Two
            deck.add(new Draw2Card(color));
            deck.add(new Draw2Card(color));
        }

        // Cuatro Wild
        for (int i = 0; i < 4; i++) {
            deck.add(new WildCard());
        }

        // Mezclar aleatoriamente
        Collections.shuffle(deck);
        return deck;
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
