package UNO;
import java.util.*;

public class Game {
    // atributos
    public  HashMap<String, Player>     players  ;
    private ArrayList<Card>             pit      ;
    private LinkedList<Card>            deck     ;
    private boolean                     gameOver ;
    private String                      winner   ;
    private LinkedList<String>          turns    ;

    // constructor
    public Game(List<Card> newDeck, Integer amountOfCards, String ... playersNames){
        // initialize all variables
        this.players = new HashMap<>();
        this.pit = new ArrayList<>() ;
        this.deck = new LinkedList<>(newDeck) ;
        this.gameOver = false;
        this.winner = null;
        this.turns = new LinkedList<>(); // Deque (double-ended queue) se usa para ver y sacar el primero, y ponerlo al final de la cola.

        // start game
        Card cardOnTop = deck.removeFirst();
        this.pit.add(cardOnTop);

        for (String name : playersNames) {
            // Tomar las primeras 'amountOfCards' del mazo para este jugador
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < amountOfCards; i++) {
                hand.add(deck.removeFirst()); // sacar del mazo
            }

            // Crear jugador y agregarlo al mapa
            Player player = new Player(name, hand);
            players.put(name, player);

            // Agregar el turno
            turns.addLast(name);
        }
    }

    // metodos
    public Card cardOnTop(){
        return pit.getLast() ;
    }

    public Game play(String playerName, Card cardToPlay){
        if(gameOver){
            throw new RuntimeException("The game has ended");
        }

        Player lastPlayer = players.get(turns.getLast()) ;
        if(lastPlayer.getAmountCards() == 1 && !lastPlayer.cantoUNO){
            lastPlayer.drawCard(deck.removeFirst());
            lastPlayer.drawCard(deck.removeFirst());
        }

        if(!turns.getFirst().equals(playerName)){
            throw new RuntimeException("It is not " + playerName + " turn");
        }

        Player player = players.get(playerName) ;

        if(!player.getHand().contains(cardToPlay)){
            throw new RuntimeException("This player´s hand doesn't contain this card") ;
        }

        if(!cardToPlay.canBePlayedOver(this.cardOnTop())){
            throw new RuntimeException("This card cannot be played this turn");
        }

        player.playCard(cardToPlay) ;
        pit.add(cardToPlay) ;
        this.turns.addLast(playerName);
        this.turns.removeFirst() ;
        cardToPlay.applyEffect(this) ; // poner adentro de playCard del jugador.

        if(player.hasWon()){
            this.gameOver = true;
            this.winner = playerName ;
            return this ;
        }

        return this ;
    }

    public Game passOrPlayAfterDraw(String playerName){
        Player player = players.get(playerName);
        Card drawnCard = deck.removeFirst();
        player.drawCard(drawnCard);

        // Repetimos la lógica del chequeo del play acá, para evitar pasarle una carta cuando en realidad queremos pasar de turno
        if (drawnCard.canBePlayedOver(this.cardOnTop())) {
            return this.play(playerName, drawnCard);
        }

        // No puede jugarla → pasa turno
        this.turns.addLast(playerName);
        this.turns.removeFirst();
        return this;
    }


    public void PlayWildCard(String color){
        pit.removeLast();
        pit.add(new ColoredWildcard(color));
    }

    public void PlayReverseCard(){
        if(players.size() == 2){
            PlaySkipCard();
        }
        else {
            Collections.reverse(this.turns);
            this.turns.addLast(this.turns.removeFirst());
        }
    }

    public void PlaySkipCard(){
        String skippedPlayer = turns.removeFirst() ;
        this.turns.addLast(skippedPlayer) ;
    }

    public void PlayDrawTwoCard(){
        String skippedPlayer = turns.removeFirst() ;
        this.turns.addLast(skippedPlayer) ;
        Card firstCard = deck.removeFirst();
        Card secondCard = deck.removeFirst();
        players.get(skippedPlayer).getHand().add(firstCard);
        players.get(skippedPlayer).getHand().add(secondCard);
    }

    public Game cantarUNO(String playerName){
        players.get(playerName).cantarUNO();
        return this ;
    }

    public String askForWinner(){
        return winner ;
    }
}
