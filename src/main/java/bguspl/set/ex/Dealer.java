package bguspl.set.ex;

import bguspl.set.Env;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;
    
    // holds the current time
    //private long currTime = 0;

    private LinkedBlockingQueue<Player> playersWithSets;

    public boolean removingCards;

    private final long second = 1000;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        playersWithSets = new LinkedBlockingQueue<Player>(players.length);
        removingCards = true;
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
        for(Player player:players){
            Thread playerThread = new Thread(player);
            playerThread.start();
        }
        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }
        announceWinners();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
        for (Player player : players){
            player.terminate();
        }
        terminate = true;
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement
        
        if (!playersWithSets.isEmpty()){
            Player plyr = playersWithSets.poll();
            int[] cardArr = plyr.getCardArray();
            if (plyr.maybeSet){
                if (env.util.testSet(cardArr)){
                    plyr.noSet();
                    removeCardsFromSet(cardArr);
                    plyr.shouldGetAPoint = true;    // Add a point to the players score
                    placeCardsOnTable();
                }
                else{
                    plyr.shouldGetAPenalty = true;
                    plyr.noSet();
                }
            
            }
        }
    }

    // Removes cards and tokens from the table.
    private void removeCardsFromSet(int[] cards){
        removingCards = true;
        for (int card : cards){
            int slot = table.cardToSlot[card];
            table.removeCard(slot);
            env.ui.removeTokens(slot);
            removeCardsFromOtherPlayers(slot);
        }
        removingCards = false;
    }

    // Removes the cards from the players actions array.
    private void removeCardsFromOtherPlayers(int slot){
        for (Player plyr : players){
            if (plyr.isInActionsRemoveSlot(slot)){
                plyr.noSet();
                playersWithSets.remove(plyr);
            }
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement
        
        int numOfCards = table.countCards();
        if (numOfCards < 12 & !deck.isEmpty()) {
            Random rand = new Random();
            shuffle();
            while(!deck.isEmpty() & numOfCards < 12){      // As long as there are cards in the deck or a slot not empty
                int slot = rand.nextInt(env.config.tableSize);
                if(table.slotToCard[slot] == null & !deck.isEmpty()){
                    table.placeCard(deck.remove(0), slot);
                    ++numOfCards;
                }
            }
            //currTime = System.currentTimeMillis();
            
            updateTimerDisplay(true);
            removingCards = false;
        }
        
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        try {
            synchronized (this) { wait(10); }
        } catch (InterruptedException ignored) {}
        
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        //TODO implement

        if (reset){
            env.ui.setCountdown(env.config.turnTimeoutMillis, false);
            try {
                Thread.sleep(second);
            } catch (InterruptedException e) {}
            reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis ;
        }
        env.ui.setCountdown(Math.max(reshuffleTime - System.currentTimeMillis(), 0) , reshuffleTime - System.currentTimeMillis() <= env.config.turnTimeoutWarningMillis);
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
        removingCards = true;

        env.ui.removeTokens();
        for (int slot = 0; slot <= table.slotToCard.length-1; ++slot){
            if (table.slotToCard[slot] != null){
                deck.add(table.slotToCard[slot]);
                table.removeCard(slot);
                removeCardsFromOtherPlayers(slot);
            }
            
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        int numOfWinners = 0;
        int winnerScore = 0;
        for (Player player:players){
            if (player.score() > winnerScore){
                numOfWinners = 1;
                winnerScore = player.score();
            } else if (player.score() == winnerScore){
                ++numOfWinners;
            }
        }
        int[] winners = new int[numOfWinners];
        int winnerIndex = 0;
        for (int i = 0; i <= players.length - 1; ++i){
            if (players[i].score() == winnerScore){
                winners[winnerIndex] = players[i].id;
                ++winnerIndex;
            }
        }
        env.ui.announceWinner(winners);
    }

    private void shuffle(){
        Collections.shuffle(deck);
    }

    public synchronized void addPlayersWithSet(Player player){
            playersWithSets.add(player);
            
    }

    public boolean isInPlayersWithSets(Player player){
        return playersWithSets.contains(player);
    }

    // for test purposes only
    public void removeAllCardsTest(){
        removeAllCardsFromTable();
    }

    // for test purposes only
    public void placeCardsTest(){
        placeCardsOnTable();
    }
}
