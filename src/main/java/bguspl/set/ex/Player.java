package bguspl.set.ex;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Level;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * The games dealer;
     */
    private final Dealer dealer;
    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    // Queue of players actions.
    private PriorityQueue<Integer> actions;

    // Boolean if the player thinks he has a set
    public boolean maybeSet;

    // Boolean if the player diserves a point
    public boolean shouldGetAPoint;

    //Boolean if the player diserves a penalty
    public boolean shouldGetAPenalty;

    private final long second = 1000;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.dealer = dealer;
        this.table = table;
        this.id = id;
        this.human = human;
        this.actions = new PriorityQueue<Integer>(3);
        this.maybeSet = false;
        this.shouldGetAPoint = false;
        this.shouldGetAPenalty = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + "starting.");
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            // TODO implement main player loop
            point();
            penalty();
        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        // try { playerThread.join(); } catch (InterruptedException ignored) {}
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                // TODO implement player key press simulator
                Random rand = new Random();
                if(!maybeSet){
                    int slot = rand.nextInt(env.config.tableSize);
                    keyPressed(slot);
                }

                try {
                    synchronized (this) { wait(second); }
                } catch (InterruptedException ignored) {}
            }
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
        terminate = true;
        playerThread.interrupt();
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement
        if (!shouldGetAPenalty & !shouldGetAPoint & !dealer.removingCards){
            if (actions.size() <= 2 & !actions.contains(slot)){
                table.placeToken(id, slot);
                if (table.slotToCard[slot] != null){
                    actions.add(slot);  
                }
               
                if (actions.size() == 3){
                    maybeSet = true;
                    dealer.addPlayersWithSet(this);
                }
            }
        
            else if (actions.contains(slot)){
                table.removeToken(id, slot);
                actions.remove(slot);
            }
        }
        
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement
        if (shouldGetAPoint){
            env.ui.setScore(id, ++score);
            freezeAndCountSeconds(env.config.pointFreezeMillis);
            shouldGetAPoint = false;
        }
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
        if (shouldGetAPenalty){
           freezeAndCountSeconds(env.config.penaltyFreezeMillis);
           shouldGetAPenalty = false;
        }
        
    }

    private void freezeAndCountSeconds(long millies){
        env.ui.setFreeze(id, millies);
        try {
            Thread.sleep(second);
        } catch (InterruptedException e) {}

        long currTime = System.currentTimeMillis();
        while(currTime - System.currentTimeMillis() + millies >= second){
            env.ui.setFreeze(id, currTime - System.currentTimeMillis() + millies);
        }
        env.ui.setFreeze(id, -second);
    }

    public int score() {
        return score;
    }


    public int[] getCardArray(){
        Integer[] pqArr = actions.toArray(new Integer[3]);
        int[] arr = new int[pqArr.length];
        for (int i=0; i <= pqArr.length - 1; ++i){
            arr[i] = table.slotToCard[pqArr[i]];
        } 
        return arr;
    }

    public void noSet(){
        maybeSet = false;
    }

    public boolean isInActionsRemoveSlot(int slot){
        return actions.remove(slot);
        
    }

    public boolean actionsContainsSlot(int slot){
        return actions.contains(slot);
    }
 }
